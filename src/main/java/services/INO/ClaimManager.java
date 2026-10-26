package services.INO;

import data.ClaimIds;
import data.Identity;
import data.VerifiedClaim;
import data.VerifyRequest;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    private final List<X509Certificate> subValidatorCerts;


    public ClaimManager() {

         PrivateKey privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
         PublicKey publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"EC");
        X509Certificate cert = CertUtil.getCertificate(certFile);

        verifiedClaimSignUtil =new VerifiedClaimSignUtil(publicKey,privateKey,cert);



        inoMock1 = new NameValidator(certFile1,privateFile1);
        inoMock2 = new EmailValidator(certFile2,privateFile2);
        inoMock3 = new SSNValidator(certFile3,privateFile3);

        // sub-claims are only accepted from the three configured validators
        subValidatorCerts = java.util.Arrays.asList(CertUtil.getCertificate(certFile1),
                CertUtil.getCertificate(certFile2), CertUtil.getCertificate(certFile3));

     }


    static SecureRandom secureRandom = new SecureRandom();

    private VerifiedClaim generateSuperClaim(VerifiedClaim... verifiedClaims)
    {
        // validate tree and signature of each ino before merging
        for (VerifiedClaim claim : verifiedClaims)
        {
            if (!isValidClaim(claim, subValidatorCerts))
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

        try {
            CompletableFuture<VerifiedClaim> verifiedClaim1
                    = CompletableFuture.supplyAsync(() -> inoMock1.verifyIdentityAndGenerateClaim(request));

            CompletableFuture<VerifiedClaim> verifiedClaim2
                    = CompletableFuture.supplyAsync(() -> inoMock2.verifyIdentityAndGenerateClaim(request));

            CompletableFuture<VerifiedClaim> verifiedClaim3
                    = CompletableFuture.supplyAsync(() -> inoMock3.verifyIdentityAndGenerateClaim(request));


            VerifiedClaim superClaim = generateSuperClaim(verifiedClaim1.get(),verifiedClaim2.get(),verifiedClaim3.get());

            //System.out.println(JSONUtil.toPrettyJSON(superClaim));

            return superClaim;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return null;
    }



}
