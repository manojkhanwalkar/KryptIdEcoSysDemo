package services.clientproxy;

import constants.Constants;
import data.*;
import services.client.Connection;
import services.client.DiscoveryClient;
import services.client.wallet.SimpleWallet;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static util.ClaimValidator.convert;
import static util.ClaimValidator.isValidClaim;

public class INOConnector {


    String rootCertFile = "/home/manoj/ca/certs/ca.cert.pem";
    String interCertFile = "/home/manoj/ca/intermediate/certs/intermediate.cert.pem";


    String certFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/clientcert.pem";
    String privateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/clientkey.der";

    PublicKey publicKey;
    PrivateKey privateKey;
    X509Certificate certificate;



    Connection ino;
    Connection sp;


    CertChainValidator certChainValidator ;


    public INOConnector(String inoURL, String spURL)
    {

         publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"EC");
         privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
         certificate = CertUtil.getCertificate(certFile);



         ino = new Connection(inoURL,publicKey,privateKey,certificate);
        sp = new Connection(spURL,publicKey,privateKey,certificate);

        certChainValidator = new CertChainValidator(rootCertFile,interCertFile);

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


    public TransactionNumber verify(VerifyRequest request)
    {

        SimpleWallet wallet = SimpleWallet.createWallet();

        try {


            request.setRsaCert(CertUtil.getCertAsString(wallet.getRsaCertificate()));

            String str = JSONUtil.toJSON(request);

            System.out.println("Sending verify request " + str);


            String response = ino.send(str,"verify");

            TransactionNumber transactionNumber= (TransactionNumber) JSONUtil.fromJSON(response,TransactionNumber.class);

            wallet.setClaimId(transactionNumber.getTransactionId());

            SimpleWallet.updateKeys(wallet);

            return transactionNumber;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }


    public VerifiedClaim getClaim(TransactionNumber transactionNumber) throws Exception
    {

        SimpleWallet wallet = SimpleWallet.getWallet(transactionNumber.getTransactionId());


        String str = JSONUtil.toJSON(transactionNumber);

        System.out.println("Sending claim request " + str);


        String response = ino.send(str,"claim");

        EncryptedVerifiedClaim encryptedVerifiedClaim = (EncryptedVerifiedClaim) JSONUtil.fromJSON(response,EncryptedVerifiedClaim.class);

        System.out.println("Received encrypted claim " + encryptedVerifiedClaim);


        VerifiedClaim claim = convert(encryptedVerifiedClaim,wallet.getRsaPrivateKey());


        isValidClaim(claim);




        System.out.println("claim after decrypting " + claim);


        return claim;

    }





    public static VerifyRequest makeSampleRequest()
    {
        VerifyRequest request = new VerifyRequest();

        Identity identity = SampleData.sampleIdentityGenerator();

        request.setIdentity(identity);



        return request;
    }


    private VerifiedClaim partial(VerifiedClaim claim)
    {
           Set<AttributeNode> nodes = new HashSet<>();

        AttributeNode attribNode1 = new AttributeNode();
        attribNode1.setName("FirstName");

        AttributeNode attribNode2 = new AttributeNode();
        attribNode2.setName("LastName");


        nodes.addAll(List.of(attribNode1,attribNode2));
        MerkleTree partialTree = MerkleTreeUtil.buildPartial(claim.getTree(),nodes);

        claim.setTree(partialTree);

        return claim;

    }

    public TransactionStatus transact(VerifiedClaim claim) throws Exception
    {

        // create partial claim for presenting to sp

        claim = partial(claim);


        String certStr = sp.get("claimkey");

        System.out.println("Got public key cert from service provider " + certStr);

        boolean result = certChainValidator.validate(certStr);

        System.out.println("############## Service provider cert is " +  result);




        PublicKey publicKey = CertUtil.getCertFromString(certStr).getPublicKey();

       // EncryptedVerifiedClaim encryptedVerifiedClaim = convert(claim,spPublicKey);

        EncryptedVerifiedClaim encryptedVerifiedClaim = convert(claim,publicKey);

        //String claimStr = JSONUtil.toJSON(claim);

      //  System.out.println("Sending partial pii claim to service provider " + claimStr);
        String encryptedVerifiedClaimStr = JSONUtil.toJSON(encryptedVerifiedClaim);

        String response = sp.send(encryptedVerifiedClaimStr,"transact");

        System.out.println("And the credit result is " + response);

        TransactionStatus transactionStatus = (TransactionStatus) JSONUtil.fromJSON(response,TransactionStatus.class);
        return transactionStatus;

    }




public static INOConnector buildINOConnector()
{
    try {
        DiscoveryClient discoveryClient = new DiscoveryClient(Constants.discoveryServiceUrl);

        String inoUrl = discoveryClient.service(Constants.INOServiceType).getServiceUrl();

        String spUrl = discoveryClient.service(Constants.SPServiceType).getServiceUrl();

        System.out.println("INO service available at " + inoUrl);

        System.out.println("SP service available at " + spUrl);

        //    ClientVerifier verifier = new ClientVerifier("https://172.17.0.1:8180/", "https://172.17.0.1:8380/");

        INOConnector connector = new INOConnector(inoUrl,spUrl);

        return connector;
    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;

}









    private static boolean isValid(VerifiedClaim claim)
    {
        LocalDateTime current = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(VerifiedClaim.pattern);

        LocalDateTime claimValidity = LocalDateTime.parse(claim.getValidUntil(), formatter);

        boolean isAfter = claimValidity.isAfter(current);

        return isAfter;

    }



}
