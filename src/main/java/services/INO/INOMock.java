package services.INO;

import data.Identity;
import data.VerifiedClaim;
import data.VerifyRequest;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.CryptUtil;
import util.MerkleTreeUtil;
import util.VerifiedClaimSignUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.List;

public abstract class INOMock {

    String certFile , privateFile;

    VerifiedClaimSignUtil verifiedClaimSignUtil ;




    public INOMock(String certFile1, String privateFile1) {

        this.certFile = certFile1;
        this.privateFile = privateFile1;

        PrivateKey privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
        PublicKey publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"EC");

        verifiedClaimSignUtil =new VerifiedClaimSignUtil(publicKey,privateKey);

    }


    static SecureRandom secureRandom = new SecureRandom();


    public abstract VerifiedClaim verifyIdentityAndGenerateClaim(VerifyRequest request);



}
