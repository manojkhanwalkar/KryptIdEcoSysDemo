package util;


import cache.SessionKeyCache;
import data.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static util.CryptUtil.secureRandom;

/**
 * Server side of the signed + encrypted message protocol.
 *
 * Security properties enforced here (all fail closed):
 *  - every request must belong to a session established by /keyexchange;
 *  - the request must be signed (ECDSA container and JWS over the same ciphertext) by the very
 *    certificate that established that session, so a third party cannot inject messages into, or
 *    re-key, someone else's session;
 *  - undecryptable requests are rejected rather than processed as plaintext.
 */
public class KeyExchangeManager {

    private static final Logger LOG = Logger.getLogger(KeyExchangeManager.class.getName());

    /** Upper bound on cached sessions, so key exchanges cannot exhaust memory; the oldest sessions are evicted first. */
    static final int MAX_SESSIONS = 10_000;

    private final java.util.Queue<String> sessionOrder = new java.util.concurrent.ConcurrentLinkedQueue<>();

    SessionKeyCache sessionKeyCache = new SessionKeyCache();

    /** clientId -> base64 certificate that performed the key exchange for that session. */
    private final Map<String, String> sessionCerts = new ConcurrentHashMap<>();

    PublicKey publicKey;
    PrivateKey privateKey;
    X509Certificate certificate;

    SignUtil digiSig ;

    SignUtil verifySig ;

    public KeyExchangeManager(PublicKey publicKey, PrivateKey privateKey, X509Certificate certificate)
    {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.certificate = certificate;

        digiSig = new SignUtil(publicKey,privateKey,certificate);
        verifySig = new SignUtil(publicKey,privateKey,certificate);
    }


    private static WebApplicationException reject(Response.Status status, String reason)
    {
        LOG.warning("Rejected request: " + reason);
        return new WebApplicationException(reason, status);
    }


    public KeyExchange processExchange(KeyExchange request) {

        if (request == null || request.getClientId() == null || request.getClientId().isEmpty()
                || request.getCertStr() == null)
            throw reject(Response.Status.BAD_REQUEST, "malformed key exchange");

        String responseCertStr = request.getCertStr();
        X509Certificate peerCert;
        try {
            peerCert = CertUtil.getCertFromString(responseCertStr);
        } catch (IllegalArgumentException e) {
            peerCert = null;
        }
        if (peerCert == null)
            throw reject(Response.Status.BAD_REQUEST, "invalid certificate in key exchange");

        try {
            peerCert.checkValidity();
        } catch (Exception e) {
            throw reject(Response.Status.UNAUTHORIZED, "expired or not yet valid certificate in key exchange");
        }

        // A clientId may only be re-keyed by the certificate that owns it.
        String existing = sessionCerts.get(request.getClientId());
        if (existing != null && !existing.equals(responseCertStr))
            throw reject(Response.Status.FORBIDDEN, "clientId already bound to a different certificate");

        DHUtil util = new DHUtil(publicKey,privateKey);
        try {
            util.setReceiverPublicKey(peerCert.getPublicKey());
        } catch (SecurityException e) {
            throw reject(Response.Status.BAD_REQUEST, "key agreement failed");
        }

        KeyExchange exchange = new KeyExchange();

        String certStr = CertUtil.getCertAsString(certificate);
        exchange.setCertStr(certStr);

        synchronized (sessionOrder) {
            String current = sessionCerts.get(request.getClientId());
            if (current != null && !current.equals(responseCertStr))
                throw reject(Response.Status.FORBIDDEN, "clientId already bound to a different certificate");
            if (current == null) {
                sessionOrder.add(request.getClientId());
                while (sessionOrder.size() > MAX_SESSIONS) {
                    String evicted = sessionOrder.poll();
                    sessionCerts.remove(evicted);
                    sessionKeyCache.remove(evicted);
                }
            }
            sessionCerts.put(request.getClientId(), responseCertStr);
            sessionKeyCache.put(request.getClientId(),util);
        }

        return exchange;

    }


    private DHUtil session(String clientId)
    {
        DHUtil util = clientId == null ? null : sessionKeyCache.get(clientId);
        if (util == null)
            throw reject(Response.Status.UNAUTHORIZED, "unknown session");
        return util;
    }


    public String decryptRequest(EncryptedMessage request)
    {
        DHUtil util = session(request.getClientId());
        try {
            return decrypt(request,util);
        } catch (SecurityException | IllegalArgumentException e) {
            throw reject(Response.Status.BAD_REQUEST, "request could not be decrypted");
        }
    }


    public String decryptRequest(JavaWebEncryptedMessage request)
    {
        DHUtil util = session(request.getClientId());
        try {
            return decrypt(request,util);
        } catch (SecurityException | IllegalArgumentException e) {
            throw reject(Response.Status.BAD_REQUEST, "request could not be decrypted");
        }
    }


    public EncryptedMessage encryptResponse(String response, EncryptedMessage request)
    {
        DHUtil util = session(request.getClientId());

        return encrypt(response,util);

    }

    public JavaWebEncryptedMessage encryptResponse(String response, JavaWebEncryptedMessage request)
    {
        DHUtil util = session(request.getClientId());

        return encryptJWE(response,util);

    }


    private String decrypt(EncryptedMessage request, DHUtil util)
    {
        if (request.getMessage() == null || request.getIv() == null)
            throw new SecurityException("missing ciphertext or iv");
        return util.decrypt(request.getMessage(),request.getIv());
    }

    private String decrypt(JavaWebEncryptedMessage request, DHUtil util)
    {
        if (request.getMessage() == null)
            throw new SecurityException("missing ciphertext");
        return util.decryptJWE(request.getMessage());
    }

    private JavaWebEncryptedMessage encryptJWE(String str, DHUtil util)
    {

        String estr = util.encryptJWE(str);
        JavaWebEncryptedMessage message = new JavaWebEncryptedMessage();
        message.setMessage(estr);
        return message;
    }

    private EncryptedMessage encrypt(String str, DHUtil util)
    {
        byte[] iv = new byte[128/8];
        secureRandom.nextBytes(iv);

        String estr = util.encrypt(str,iv);
        EncryptedMessage message = new EncryptedMessage();
        message.setMessage(estr);
        message.setIv(Base64.getEncoder().encodeToString(iv));
        return message;
    }


    private void requireSessionCert(String clientId, String signerCertStr)
    {
        String expected = clientId == null ? null : sessionCerts.get(clientId);
        if (expected == null)
            throw reject(Response.Status.UNAUTHORIZED, "unknown session");
        if (signerCertStr == null || !MessageDigest.isEqual(expected.getBytes(), signerCertStr.getBytes()))
            throw reject(Response.Status.UNAUTHORIZED, "message not signed by the session certificate");
    }


    /**
     * Verifies the request signature; throws (HTTP 401) if it is missing or invalid.
     * @return true when the signature is valid (never returns false)
     */
    public boolean verify(JavaWebEncryptedMessage encryptedMessage) {
        if (encryptedMessage == null || encryptedMessage.getContainer() == null)
            throw reject(Response.Status.UNAUTHORIZED, "missing signature");

        requireSessionCert(encryptedMessage.getClientId(), encryptedMessage.getContainer().getCertStr());

        boolean valid;
        try {
            valid = encryptedMessage.getMessage() != null
                    && verifySig.verify(encryptedMessage.getContainer(),encryptedMessage.getMessage());
        } catch (Exception e) {
            valid = false;
        }
        if (!valid)
            throw reject(Response.Status.UNAUTHORIZED, "invalid message signature");
        return true;
    }


    /**
     * Verifies both the ECDSA container and the JWS over the ciphertext; throws (HTTP 401) on failure.
     * @return true when the signatures are valid (never returns false)
     */
    public boolean verify(EncryptedMessage encryptedMessage) {
        if (encryptedMessage == null || encryptedMessage.getContainer() == null || encryptedMessage.getJwsContainer() == null)
            throw reject(Response.Status.UNAUTHORIZED, "missing signature");

        requireSessionCert(encryptedMessage.getClientId(), encryptedMessage.getContainer().getCertStr());
        requireSessionCert(encryptedMessage.getClientId(), encryptedMessage.getJwsContainer().getCertStr());

        boolean valid;
        try {
            valid = encryptedMessage.getMessage() != null
                    && verifySig.verify(encryptedMessage.getContainer(),encryptedMessage.getMessage())
                    && verifySig.verifyJWS(encryptedMessage.getJwsContainer(), encryptedMessage.getMessage());
        } catch (Exception e) {
            valid = false;
        }
        if (!valid)
            throw reject(Response.Status.UNAUTHORIZED, "invalid message signature");
        return true;
    }


    public void sign(EncryptedMessage message) {

        try {
            SignatureContainer obj = digiSig.sign(message.getMessage());

            message.setContainer(obj);

            JavaWebSignatureContainer obj1 = digiSig.jwsSign(message.getMessage());
            message.setJwsContainer(obj1);

        } catch (GeneralSecurityException | java.io.UnsupportedEncodingException e) {
            // fail closed: never return an unsigned response
            throw new IllegalStateException("Signing failed", e);
        }

    }


    public void sign(JavaWebEncryptedMessage message) {

        try {
            SignatureContainer obj = digiSig.sign(message.getMessage());

            message.setContainer(obj);

        } catch (GeneralSecurityException | java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("Signing failed", e);
        }

    }
}
