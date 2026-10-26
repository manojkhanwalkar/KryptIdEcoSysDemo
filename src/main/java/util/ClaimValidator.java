package util;

import data.EncryptedVerifiedClaim;
import data.VerifiedClaim;

import constants.Constants;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClaimValidator {

    /** Certificate of the INO key that signs the combined ("super") claims handed to clients, SPs and bureaus. */
    static final String CLAIM_ISSUER_CERT_FILE = Constants.FileLocation + "KryptIdEcoSysDemo/src/main/resources/claimkeycertificate.pem";

    private static volatile List<X509Certificate> trustedClaimIssuers;

    static List<X509Certificate> trustedClaimIssuers()
    {
        if (trustedClaimIssuers == null) {
            X509Certificate cert = CertUtil.getCertificate(CLAIM_ISSUER_CERT_FILE);
            trustedClaimIssuers = cert == null ? Collections.emptyList() : Collections.singletonList(cert);
        }
        return trustedClaimIssuers;
    }

    /**
     * Validates a claim issued by the INO: the signer certificate must be the pinned INO claim-issuer
     * certificate (a certificate supplied inside the claim proves nothing on its own), the signature must
     * cover the tree root, and every hash in the tree must be consistent. Fails closed.
     */
    public static boolean isValidClaim(VerifiedClaim claim)
    {
        return isValidClaim(claim, trustedClaimIssuers());
    }

    /** As {@link #isValidClaim(VerifiedClaim)}, but against an explicit set of trusted signer certificates. */
    public static boolean isValidClaim(VerifiedClaim claim, Collection<X509Certificate> trustedIssuers)
    {
        if (claim == null || claim.getTree() == null || claim.getTree().getRoot() == null
                || claim.getSignature() == null || claim.getCertStr() == null
                || trustedIssuers == null || trustedIssuers.isEmpty())
            return false;

        try {
            X509Certificate signer = CertUtil.getCertFromString(claim.getCertStr());
            if (signer == null || !isTrusted(signer, trustedIssuers))
                return false;

            if (!MerkleTreeUtil.validateTree(claim.getTree()))
                return false;

            VerifiedClaimSignUtil signUtil = new VerifiedClaimSignUtil(claim.getCertStr());
            return signUtil.verify(claim.getSignature(), claim.getTree().getRoot().getHash());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean isTrusted(X509Certificate signer, Collection<X509Certificate> trustedIssuers)
    {
        for (X509Certificate trusted : trustedIssuers) {
            try {
                if (trusted != null && MessageDigest.isEqual(trusted.getEncoded(), signer.getEncoded()))
                    return true;
            } catch (CertificateEncodingException e) {
                // ignore this entry
            }
        }
        return false;
    }

    /** Throws if the claim is not valid; use where processing must stop on an invalid claim. */
    public static VerifiedClaim requireValidClaim(VerifiedClaim claim)
    {
        if (!isValidClaim(claim))
            throw new SecurityException("Claim failed validation (untrusted issuer, bad signature or tampered tree)");
        return claim;
    }

   public static VerifiedClaim convert(EncryptedVerifiedClaim encryptedVerifiedClaim, PrivateKey rsaPrivateKey)
    {
        if (encryptedVerifiedClaim == null || encryptedVerifiedClaim.getVerifiedClaim() == null || encryptedVerifiedClaim.getEncryptedKey() == null)
            throw new SecurityException("Missing encrypted claim");
        String encclaimStr = encryptedVerifiedClaim.getVerifiedClaim();
        String key = encryptedVerifiedClaim.getEncryptedKey();
        String claimStr = CryptUtil.decrypt(encclaimStr,key,rsaPrivateKey);
        if (claimStr == null)
            throw new SecurityException("Claim could not be decrypted");

        VerifiedClaim claim = (VerifiedClaim) JSONUtil.fromJSON(claimStr,VerifiedClaim.class);

        return claim;


    }

    public static EncryptedVerifiedClaim convert(VerifiedClaim verifiedClaim, PublicKey rsaPublicKey)
    {
        String claimStr = JSONUtil.toJSON(verifiedClaim);

        EncryptedVerifiedClaim encryptedVerifiedClaim = new EncryptedVerifiedClaim();

       // PublicKey rsaPublicKey = CryptUtil.convertStringtoPublicKey(rsaPublicKeyStr,"RSA");

        SymKeyStringTuple tuple = CryptUtil.encrypt(claimStr,rsaPublicKey);

        encryptedVerifiedClaim.setVerifiedClaim(tuple.message);
        encryptedVerifiedClaim.setEncryptedKey(tuple.key);

        return encryptedVerifiedClaim;

    }


    public static EncryptedVerifiedClaim convert(VerifiedClaim verifiedClaim, String rsaPublicKeyStr)
    {
        String claimStr = JSONUtil.toJSON(verifiedClaim);

        EncryptedVerifiedClaim encryptedVerifiedClaim = new EncryptedVerifiedClaim();

        PublicKey rsaPublicKey = CryptUtil.convertStringtoPublicKey(rsaPublicKeyStr,"RSA");

        SymKeyStringTuple tuple = CryptUtil.encrypt(claimStr,rsaPublicKey);

        encryptedVerifiedClaim.setVerifiedClaim(tuple.message);
        encryptedVerifiedClaim.setEncryptedKey(tuple.key);

        return encryptedVerifiedClaim;

    }

}
