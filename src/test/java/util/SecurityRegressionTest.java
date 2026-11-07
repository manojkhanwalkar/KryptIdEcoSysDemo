package util;

import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import data.EncryptedMessage;
import data.KeyExchange;
import data.VerifiedClaim;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Test;
import services.discovery.DiscoveryResource;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;

import javax.ws.rs.WebApplicationException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/** Regression tests for the OWASP Top 10 audit fixes (see security/OWASP-AUDIT.md). */
public class SecurityRegressionTest {

    static final class Identity {
        final KeyPair keys;
        final X509Certificate cert;

        Identity(String cn) throws Exception {
            KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
            g.initialize(new ECGenParameterSpec("secp256r1"));
            keys = g.generateKeyPair();
            X500Name name = new X500Name("CN=" + cn);
            Date now = new Date();
            JcaX509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(name, BigInteger.valueOf(System.nanoTime()),
                    new Date(now.getTime() - 60_000), new Date(now.getTime() + 3_600_000), name, keys.getPublic());
            cert = new JcaX509CertificateConverter().getCertificate(
                    b.build(new JcaContentSignerBuilder("SHA256withECDSA").build(keys.getPrivate())));
        }

        String certStr() {
            return CertUtil.getCertAsString(cert);
        }
    }

    // --- A08: polymorphic deserialization is restricted to application types ---

    @Test
    public void typeValidatorRejectsGadgetClasses() throws Exception {
        PolymorphicTypeValidator ptv = JSONUtil.TYPE_VALIDATOR;
        assertNotEquals(PolymorphicTypeValidator.Validity.ALLOWED,
                ptv.validateSubClassName(null, null, "com.sun.rowset.JdbcRowSetImpl"));
        assertNotEquals(PolymorphicTypeValidator.Validity.ALLOWED,
                ptv.validateSubClassName(null, null, "org.springframework.context.support.FileSystemXmlApplicationContext"));
        assertEquals(PolymorphicTypeValidator.Validity.ALLOWED,
                ptv.validateSubClassName(null, null, "shareablemerkletree.HashNode"));
    }

    private static MerkleTree sampleTree() {
        return sampleTree("Ada");
    }

    private static MerkleTree sampleTree(String firstName) {
        AttributeNode a = new AttributeNode();
        a.setName("FirstName"); a.setValue(firstName); a.setId(RandomIdGenerator.getId());
        AttributeNode b = new AttributeNode();
        b.setName("LastName"); b.setValue("Lovelace"); b.setId(RandomIdGenerator.getId());
        return new MerkleTreeUtil().build(List.of(a, b));
    }

    @Test
    public void claimTreeStillRoundTripsThroughJson() {
        VerifiedClaim claim = new VerifiedClaim();
        claim.setTree(sampleTree());
        VerifiedClaim back = (VerifiedClaim) JSONUtil.fromJSON(JSONUtil.toJSON(claim), VerifiedClaim.class);
        assertNotNull(back);
        assertTrue(MerkleTreeUtil.validateTree(back.getTree()));
        assertEquals(claim.getTree().getRoot().getHash(), back.getTree().getRoot().getHash());
    }

    // --- A06/A07: claims must be signed by a pinned issuer ---

    private static VerifiedClaim signedClaim(Identity issuer) {
        VerifiedClaim claim = new VerifiedClaim();
        claim.setTree(sampleTree());
        VerifiedClaimSignUtil signer = new VerifiedClaimSignUtil(issuer.keys.getPublic(), issuer.keys.getPrivate(), issuer.cert);
        claim.setSignature(signer.sign(claim.getTree().getRoot().getHash()));
        claim.setCertStr(issuer.certStr());
        return claim;
    }

    @Test
    public void claimFromTrustedIssuerIsAccepted() throws Exception {
        Identity ino = new Identity("ino");
        assertTrue(ClaimValidator.isValidClaim(signedClaim(ino), Collections.singletonList(ino.cert)));
    }

    @Test
    public void selfSignedForgedClaimIsRejected() throws Exception {
        Identity ino = new Identity("ino");
        Identity attacker = new Identity("attacker");
        assertFalse(ClaimValidator.isValidClaim(signedClaim(attacker), Collections.singletonList(ino.cert)));
    }

    @Test
    public void tamperedClaimIsRejected() throws Exception {
        Identity ino = new Identity("ino");
        VerifiedClaim claim = signedClaim(ino);
        claim.setTree(sampleTree("Mallory")); // attributes swapped after signing
        assertFalse(ClaimValidator.isValidClaim(claim, Collections.singletonList(ino.cert)));
        assertFalse(ClaimValidator.isValidClaim(null, Collections.singletonList(ino.cert)));
    }

    // --- A04/A07: HMAC compare ---

    @Test
    public void hmacValidation() {
        String key = "0123456789abcdef0123456789abcdef";
        String mac = HMACUtil.calculateHMAC("body", key);
        assertTrue(HMACUtil.isValid(mac, "body", key));
        assertFalse(HMACUtil.isValid(mac, "other", key));
        assertFalse(HMACUtil.isValid(null, "body", key));
        assertFalse(HMACUtil.isValid(mac, "body", null));
    }

    // --- A10: crypto helpers fail closed ---

    @Test(expected = SecurityException.class)
    public void decryptWithWrongSessionKeyFailsClosed() throws Exception {
        Identity a = new Identity("a"), b = new Identity("b"), c = new Identity("c");
        DHUtil ab = new DHUtil(a.keys.getPublic(), a.keys.getPrivate());
        ab.setReceiverPublicKey(b.keys.getPublic());
        DHUtil cb = new DHUtil(c.keys.getPublic(), c.keys.getPrivate());
        cb.setReceiverPublicKey(b.keys.getPublic());
        byte[] iv = new byte[16];
        String ct = ab.encrypt("secret", iv);
        cb.decrypt(ct, Base64.getEncoder().encodeToString(iv));
    }

    @Test(expected = SecurityException.class)
    public void plaintextIsNotAcceptedAsCiphertext() throws Exception {
        Identity a = new Identity("a"), b = new Identity("b");
        DHUtil ab = new DHUtil(a.keys.getPublic(), a.keys.getPrivate());
        ab.setReceiverPublicKey(b.keys.getPublic());
        ab.decrypt("{\"transactionId\":\"x\"}", Base64.getEncoder().encodeToString(new byte[16]));
    }

    // --- A07: request signatures enforced and bound to the session certificate ---

    private static EncryptedMessage clientMessage(String clientId, Identity client, Identity server, String body) throws Exception {
        DHUtil dh = new DHUtil(client.keys.getPublic(), client.keys.getPrivate());
        dh.setReceiverPublicKey(server.keys.getPublic());
        byte[] iv = new byte[16];
        EncryptedMessage m = new EncryptedMessage();
        m.setClientId(clientId);
        m.setIv(Base64.getEncoder().encodeToString(iv));
        m.setMessage(dh.encrypt(body, iv));
        SignUtil s = new SignUtil(client.keys.getPublic(), client.keys.getPrivate(), client.cert);
        m.setContainer(s.sign(m.getMessage()));
        m.setJwsContainer(s.jwsSign(m.getMessage()));
        return m;
    }

    private static KeyExchangeManager serverWithSession(Identity server, String clientId, Identity client) {
        KeyExchangeManager kem = new KeyExchangeManager(server.keys.getPublic(), server.keys.getPrivate(), server.cert);
        KeyExchange ke = new KeyExchange();
        ke.setClientId(clientId);
        ke.setCertStr(client.certStr());
        kem.processExchange(ke);
        return kem;
    }

    @Test
    public void validSessionMessageIsAccepted() throws Exception {
        Identity server = new Identity("server"), client = new Identity("client");
        String id = UUID.randomUUID().toString();
        KeyExchangeManager kem = serverWithSession(server, id, client);
        EncryptedMessage m = clientMessage(id, client, server, "hello");
        assertTrue(kem.verify(m));
        assertEquals("hello", kem.decryptRequest(m));
    }

    @Test
    public void messageSignedByAnotherCertificateIsRejected() throws Exception {
        Identity server = new Identity("server"), client = new Identity("client"), attacker = new Identity("attacker");
        String id = UUID.randomUUID().toString();
        KeyExchangeManager kem = serverWithSession(server, id, client);
        EncryptedMessage m = clientMessage(id, attacker, server, "hello");
        try {
            kem.verify(m);
            fail("expected rejection");
        } catch (WebApplicationException e) {
            assertEquals(401, e.getResponse().getStatus());
        }
    }

    @Test
    public void unsignedOrTamperedMessageIsRejected() throws Exception {
        Identity server = new Identity("server"), client = new Identity("client");
        String id = UUID.randomUUID().toString();
        KeyExchangeManager kem = serverWithSession(server, id, client);

        EncryptedMessage unsigned = clientMessage(id, client, server, "hello");
        unsigned.setContainer(null);
        try { kem.verify(unsigned); fail(); } catch (WebApplicationException e) { assertEquals(401, e.getResponse().getStatus()); }

        EncryptedMessage tampered = clientMessage(id, client, server, "hello");
        tampered.setMessage(clientMessage(id, client, server, "other").getMessage());
        try { kem.verify(tampered); fail(); } catch (WebApplicationException e) { assertEquals(401, e.getResponse().getStatus()); }

        EncryptedMessage unknown = clientMessage("no-such-session", client, server, "hello");
        try { kem.verify(unknown); fail(); } catch (WebApplicationException e) { assertEquals(401, e.getResponse().getStatus()); }
    }

    @Test
    public void sessionCannotBeTakenOverByAnotherCertificate() throws Exception {
        Identity server = new Identity("server"), client = new Identity("client"), attacker = new Identity("attacker");
        String id = UUID.randomUUID().toString();
        KeyExchangeManager kem = serverWithSession(server, id, client);
        KeyExchange ke = new KeyExchange();
        ke.setClientId(id);
        ke.setCertStr(attacker.certStr());
        try {
            kem.processExchange(ke);
            fail("expected rejection");
        } catch (WebApplicationException e) {
            assertEquals(403, e.getResponse().getStatus());
        }
    }

    // --- A01: discovery only accepts allow-listed https service URLs ---

    @Test
    public void discoveryUrlAllowList() {
        assertTrue(DiscoveryResource.isAllowedUrl("https://172.17.0.1:8480/"));
        assertFalse(DiscoveryResource.isAllowedUrl("https://attacker.example:8480/"));
        assertFalse(DiscoveryResource.isAllowedUrl("http://172.17.0.1:8480/"));
        assertFalse(DiscoveryResource.isAllowedUrl("https://169.254.169.254/latest/meta-data/"));
        assertFalse(DiscoveryResource.isAllowedUrl("file:///etc/passwd"));
        assertFalse(DiscoveryResource.isAllowedUrl(null));
    }
}
