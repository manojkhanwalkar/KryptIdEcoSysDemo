package services.INO;

import data.Identity;
import data.VerifiedClaim;
import data.VerifyRequest;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.CertUtil;
import util.CryptUtil;
import util.MerkleTreeUtil;
import util.VerifiedClaimSignUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import static data.Identity.PIIAttributes;


public abstract class INOMock {

    String certFile , privateFile;

    VerifiedClaimSignUtil verifiedClaimSignUtil ;




    public INOMock(String certFile1, String privateFile1) {

        this.certFile = certFile1;
        this.privateFile = privateFile1;

        PrivateKey privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
        PublicKey publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"EC");
        X509Certificate cert = CertUtil.getCertificate(certFile);

        verifiedClaimSignUtil =new VerifiedClaimSignUtil(publicKey,privateKey,cert);

    }


    static SecureRandom secureRandom = new SecureRandom();


    public abstract VerifiedClaim verifyIdentityAndGenerateClaim(VerifyRequest request);


    protected VerifiedClaim createClaim(MerkleTree tree)
    {
        VerifiedClaim verifiedClaim = new VerifiedClaim();
        verifiedClaim.setTree(tree);

        String toSign = tree.getRoot().getHash();

        String signature = verifiedClaimSignUtil.sign(toSign);


        verifiedClaim.setSignature(signature);

       // String publicKeyStr = CryptUtil.convertPublicKeyToString(verifiedClaimSignUtil.getPublicKey());

        verifiedClaim.setCertStr(CertUtil.getCertAsString(verifiedClaimSignUtil.getCert()));

        return verifiedClaim;

    }



}
