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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static util.ClaimValidator.isValidClaim;

public class ClaimManager {


    static String certFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/claimkeycertificate.pem";
    static String privateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/claimkey.der";


    static String certFile1 = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/ino1claimkeycertificate.pem";
    static String privateFile1 = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/ino1claimkey.der";

    static String certFile2 = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/ino2claimkeycertificate.pem";
    static String privateFile2 = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/ino2claimkey.der";

    static String certFile3 = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/ino3claimkeycertificate.pem";
    static String privateFile3 = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/ino3claimkey.der";




    VerifiedClaimSignUtil verifiedClaimSignUtil ;

    INOMock inoMock1, inoMock2, inoMock3;


    public ClaimManager() {

         PrivateKey privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
         PublicKey publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"EC");
        X509Certificate cert = CertUtil.getCertificate(certFile);

        verifiedClaimSignUtil =new VerifiedClaimSignUtil(publicKey,privateKey,cert);



        inoMock1 = new NameValidator(certFile1,privateFile1);
        inoMock2 = new EmailValidator(certFile2,privateFile2);
        inoMock3 = new SSNValidator(certFile3,privateFile3);

     }


    static SecureRandom secureRandom = new SecureRandom();

    private VerifiedClaim generateSuperClaim(VerifiedClaim... verifiedClaims)
    {
        // validate tree and signature of each ino before merging
        for (VerifiedClaim claim : verifiedClaims)
        {
            if (!isValidClaim(claim))
                throw new RuntimeException("Claim is invalid");
        }

        MerkleTree[] trees = new MerkleTree[verifiedClaims.length];
        int index=0;
        for (VerifiedClaim claim : verifiedClaims)
        {
            trees[index++] = claim.getTree();
        }
        MerkleTree tree = MerkleTreeUtil.merge(trees);

        VerifiedClaim verifiedClaim = new VerifiedClaim();
        verifiedClaim.setTree(tree);

        String toSign = tree.getRoot().getHash();

        String signature = verifiedClaimSignUtil.sign(toSign);

        LocalDateTime dateTime = LocalDateTime.now(); // gets the current date and time
        dateTime = dateTime.plusMinutes(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(VerifiedClaim.pattern);
        verifiedClaim.setValidUntil(dateTime.format(formatter));

      //  System.out.println(verifiedClaim.getValidUntil());



      //  dateTime = LocalDateTime.parse(dateTime.format(formatter), formatter);



       // System.out.println(dateTime.format(formatter));



        verifiedClaim.setSignature(signature);

       // String publicKeyStr = CryptUtil.convertPublicKeyToString(verifiedClaimSignUtil.getPublicKey());

        verifiedClaim.setCertStr(CertUtil.getCertAsString(verifiedClaimSignUtil.getCert()));

        return verifiedClaim;


    }



    public VerifiedClaim verifyIdentityAndGenerateClaim(VerifyRequest request)
    {

        VerifiedClaim verifiedClaim1 = inoMock1.verifyIdentityAndGenerateClaim(request);
        VerifiedClaim verifiedClaim2 = inoMock2.verifyIdentityAndGenerateClaim(request);
        VerifiedClaim verifiedClaim3 = inoMock3.verifyIdentityAndGenerateClaim(request);

        VerifiedClaim superClaim = generateSuperClaim(verifiedClaim1,verifiedClaim2,verifiedClaim3);

        return superClaim;

    }


}
