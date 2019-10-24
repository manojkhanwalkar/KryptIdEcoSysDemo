package services.client;

import data.*;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.CryptUtil;
import util.JSONUtil;
import util.MerkleTreeUtil;
import util.SampleData;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static util.ClaimValidator.isValidClaim;

public class ClientVerifierOld {

/*    String certFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/clientcert.pem";
    String privateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/clientkey.der";

    PublicKey publicKey;
    PrivateKey privateKey;


    String rsaCertFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/userrsacertificate.pem";
    String rsaPrivateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/userrsakey.der";

    PublicKey rsaPublicKey;
    PrivateKey rsaPrivateKey;

    Connection ino;
    Connection sp;
    public ClientVerifierOld(String inoURL, String spURL)
    {

         publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"EC");
         privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");

         rsaPublicKey = CryptUtil.getPublicKeyFromCertFile(rsaCertFile,"RSA");
         rsaPrivateKey = CryptUtil.getPrivateKeyDerFromFile(rsaPrivateFile,"RSA");

         ino = new Connection(inoURL,publicKey,privateKey);
        sp = new Connection(spURL,publicKey,privateKey);

        init();

    }

    private void init()
    {
        try {
            ino.connect();
            sp.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public TransactionNumber verify() throws Exception
    {


        VerifyRequest request = makeSampleRequest();

        request.setRsaPublicKey(CryptUtil.convertPublicKeyToString(rsaPublicKey));

        String str = JSONUtil.toJSON(request);

        System.out.println("Sending verify request " + str);


        String response = ino.send(str,"verify");

        TransactionNumber transactionNumber= (TransactionNumber) JSONUtil.fromJSON(response,TransactionNumber.class);

        return transactionNumber;


    }


    public VerifiedClaim getClaim(TransactionNumber transactionNumber) throws Exception
    {



        String str = JSONUtil.toJSON(transactionNumber);

        System.out.println("Sending claim request " + str);


        String response = ino.send(str,"claim");

        EncryptedVerifiedClaim encryptedVerifiedClaim = (EncryptedVerifiedClaim) JSONUtil.fromJSON(response,EncryptedVerifiedClaim.class);

        System.out.println("Received encrypted claim " + encryptedVerifiedClaim);

        //TODO - need to decrypt the verified claim using the users private key .
        String encclaimStr = encryptedVerifiedClaim.getVerifiedClaim();
        String key = encryptedVerifiedClaim.getEncryptedKey();
        String claimStr = CryptUtil.decrypt(encclaimStr,key,rsaPrivateKey);

        VerifiedClaim claim = (VerifiedClaim) JSONUtil.fromJSON(claimStr,VerifiedClaim.class);

        isValidClaim(claim);

        System.out.println("claim after decrypting " + claim);

        Set<AttributeNode> nodes = new HashSet<>();

        AttributeNode attribNode1 = new AttributeNode();
        attribNode1.setName("FirstName");

        AttributeNode attribNode2 = new AttributeNode();
        attribNode2.setName("LastName");


        nodes.addAll(List.of(attribNode1,attribNode2));
        MerkleTree partialTree = MerkleTreeUtil.buildPartial(claim.getTree(),nodes);

        claim.setTree(partialTree);

        //transact(claim);

        return claim;

    }





    private VerifyRequest makeSampleRequest()
    {
        VerifyRequest request = new VerifyRequest();

        Identity identity = SampleData.sampleIdentityGenerator();

        request.setIdentity(identity);



        return request;
    }

    public void transact(VerifiedClaim claim) throws Exception
    {


        String claimStr = JSONUtil.toJSON(claim);

        System.out.println("Sending partial pii claim to service provider " + claimStr);

        String response = sp.send(claimStr,"transact");

        System.out.println("And the credit result is " + response);
    }









    public static void main(String[] args) throws Exception {


        ClientVerifierOld verifier = new ClientVerifierOld("https://172.17.0.1:8180/", "https://172.17.0.1:8380/");


       // verifier.exchangeKeys();

        TransactionNumber transactionNumber = verifier.verify();
        VerifiedClaim claim = verifier.getClaim(transactionNumber);

       verifier.transact(claim);

    }

*/

}
