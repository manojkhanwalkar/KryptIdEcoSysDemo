package services.client;

import constants.Constants;
import data.*;
import org.apache.commons.codec.digest.Crypt;
import services.client.wallet.SimpleWallet;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.*;

import javax.swing.table.TableRowSorter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static util.ClaimValidator.convert;
import static util.ClaimValidator.isValidClaim;

public class ClientVerifier {


    String rootCertFile = "/home/manoj/ca/certs/ca.cert.pem";
    String interCertFile = "/home/manoj/ca/intermediate/certs/intermediate.cert.pem";


    String certFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/clientcert.pem";
    String privateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/clientkey.der";

    PublicKey publicKey;
    PrivateKey privateKey;
    X509Certificate certificate;


   // String rsaCertFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/userrsacertificate.pem";
  //  String rsaPrivateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/userrsakey.der";

    PublicKey rsaPublicKey;
    PrivateKey rsaPrivateKey;
    X509Certificate rsaCertificate ;

    Connection ino;
    Connection sp;

    SimpleWallet wallet = new SimpleWallet();

    CertChainValidator certChainValidator ;


    public ClientVerifier(String inoURL, String spURL)
    {

         publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"EC");
         privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
         certificate = CertUtil.getCertificate(certFile);


        rsaPublicKey = wallet.getRsaPublicKey();
        rsaPrivateKey = wallet.getRsaPrivateKey();
        rsaCertificate = wallet.getRsaCertificate();


      //    = CryptUtil.getPublicKeyFromCertFile(rsaCertFile,"RSA");
        //  = CryptUtil.getPrivateKeyDerFromFile(rsaPrivateFile,"RSA");

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


    public TransactionNumber verify(VerifyRequest request) throws Exception
    {



        request.setRsaCert(CertUtil.getCertAsString(rsaCertificate));


        //request.setRsaPublicKey(CryptUtil.convertPublicKeyToString(rsaPublicKey));

        String str = JSONUtil.toJSON(request);

        System.out.println("Sending verify request " + str);


        String response = ino.send(str,"verify");

        TransactionNumber transactionNumber= (TransactionNumber) JSONUtil.fromJSON(response,TransactionNumber.class);

        wallet.updateClaimId(transactionNumber.getTransactionId());

        return transactionNumber;


    }


    public VerifiedClaim getClaim(TransactionNumber transactionNumber) throws Exception
    {



        String str = JSONUtil.toJSON(transactionNumber);

        System.out.println("Sending claim request " + str);


        String response = ino.send(str,"claim");

        EncryptedVerifiedClaim encryptedVerifiedClaim = (EncryptedVerifiedClaim) JSONUtil.fromJSON(response,EncryptedVerifiedClaim.class);

        System.out.println("Received encrypted claim " + encryptedVerifiedClaim);


     /*   String encclaimStr = encryptedVerifiedClaim.getVerifiedClaim();
        String key = encryptedVerifiedClaim.getEncryptedKey();
        String claimStr = CryptUtil.decrypt(encclaimStr,key,rsaPrivateKey);

        VerifiedClaim claim = (VerifiedClaim) JSONUtil.fromJSON(claimStr,VerifiedClaim.class);*/

        VerifiedClaim claim = convert(encryptedVerifiedClaim,rsaPrivateKey);

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





    private static VerifyRequest makeSampleRequest()
    {
        VerifyRequest request = new VerifyRequest();

        Identity identity = SampleData.sampleIdentityGenerator();

        request.setIdentity(identity);



        return request;
    }

    public void transact(VerifiedClaim claim) throws Exception
    {

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
    }







    public static void main(String[] args) throws Exception {

        DiscoveryClient discoveryClient = new DiscoveryClient(Constants.discoveryServiceUrl);

        String inoUrl = discoveryClient.service(Constants.INOServiceType).getServiceUrl();

        String spUrl = discoveryClient.service(Constants.SPServiceType).getServiceUrl();

        System.out.println("INO service available at " + inoUrl);

        System.out.println("SP service available at " + spUrl);

    //    ClientVerifier verifier = new ClientVerifier("https://172.17.0.1:8180/", "https://172.17.0.1:8380/");

        ClientVerifier verifier = new ClientVerifier(inoUrl,spUrl);


       // verifier.exchangeKeys();

        TransactionNumber transactionNumber;
       String claimId = verifier.wallet.getClaimId();

        if (claimId==null) {
            VerifyRequest request = makeSampleRequest();
            transactionNumber = verifier.verify(request);
        }
        else
        {
            transactionNumber = new TransactionNumber();
            transactionNumber.setTransactionId(claimId);
        }


        VerifiedClaim claim = verifier.getClaim(transactionNumber);

        if (!isValid(claim))
        {

            System.out.println("Claim has expired - requesting a new one " );
            VerifyRequest request = makeSampleRequest();
            transactionNumber = verifier.verify(request);
            claim = verifier.getClaim(transactionNumber);

     }
        else
        {
            System.out.println("Claim is valid ");
        }

       verifier.transact(claim);

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
