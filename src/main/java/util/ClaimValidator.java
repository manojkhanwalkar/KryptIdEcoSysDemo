package util;

import data.EncryptedVerifiedClaim;
import data.VerifiedClaim;

import java.security.PrivateKey;
import java.security.PublicKey;

public class ClaimValidator {

    public static boolean isValidClaim(VerifiedClaim claim)
    {

        boolean verify ;

        VerifiedClaimSignUtil signUtil = new VerifiedClaimSignUtil(claim.getCertStr());

        verify = MerkleTreeUtil.validateTree(claim.getTree());

        System.out.println("tree hash verified " + verify);

        verify = signUtil.verify(claim.getSignature(), claim.getTree().getRoot().getHash());

        System.out.println("Claim signature verified " + verify);

        return verify;

    }

   public static VerifiedClaim convert(EncryptedVerifiedClaim encryptedVerifiedClaim, PrivateKey rsaPrivateKey)
    {
        String encclaimStr = encryptedVerifiedClaim.getVerifiedClaim();
        String key = encryptedVerifiedClaim.getEncryptedKey();
        String claimStr = CryptUtil.decrypt(encclaimStr,key,rsaPrivateKey);

        System.out.println("DEBUG");
        System.out.println(claimStr);

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
