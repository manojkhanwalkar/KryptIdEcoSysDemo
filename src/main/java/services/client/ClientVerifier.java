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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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



  /*  PublicKey rsaPublicKey;
    PrivateKey rsaPrivateKey = null;
    X509Certificate rsaCertificate ; */

    Connection ino;
    Connection sp;



    CertChainValidator certChainValidator ;


    public ClientVerifier(String inoURL, String spURL)
    {

         publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"EC");
         privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
         certificate = CertUtil.getCertificate(certFile);









       /* System.out.println(Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded()));
        getPublicFromPrivate();*/



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

        SimpleWallet wallet = SimpleWallet.createWallet();


        request.setRsaCert(CertUtil.getCertAsString(wallet.getRsaCertificate()));


        //request.setRsaPublicKey(CryptUtil.convertPublicKeyToString(rsaPublicKey));

        String str = JSONUtil.toJSON(request);

        System.out.println("Sending verify request " + str);


        String response = ino.send(str,"verify");

        TransactionNumber transactionNumber= (TransactionNumber) JSONUtil.fromJSON(response,TransactionNumber.class);

        wallet.setClaimId(transactionNumber.getTransactionId());

        SimpleWallet.updateKeys(wallet);

        return transactionNumber;


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

    /*    TransactionNumber transactionNumber;
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

       verifier.transact(claim); */

//       for (int i=0;i<32;i++) {

            TransactionNumber transactionNumber;
            VerifyRequest request = makeSampleRequest();

            System.out.println(JSONUtil.toJSON(request));

            transactionNumber = verifier.verify(request);
            VerifiedClaim claim = verifier.getClaim(transactionNumber);

            System.out.println(JSONUtil.toPrettyJSON(claim));

            verifier.transact(claim);


            //Thread.sleep(2000);
    //   }




    }


    private static boolean isValid(VerifiedClaim claim)
    {
        LocalDateTime current = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(VerifiedClaim.pattern);

        LocalDateTime claimValidity = LocalDateTime.parse(claim.getValidUntil(), formatter);

        boolean isAfter = claimValidity.isAfter(current);

        return isAfter;

    }


    /* public void getPublicFromPrivate()
    {
        RSAPrivateCrtKey rsaPrivateKey1 = (RSAPrivateCrtKey)rsaPrivateKey;

        //create a KeySpec and let the Factory due the Rest. You could also create the KeyImpl by your own.
        PublicKey publicKey = null;
        try {
            publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(rsaPrivateKey1.getModulus(), rsaPrivateKey1.getPublicExponent()));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println(Base64.getEncoder().encodeToString(publicKey.getEncoded())); //store it - that's it
    }*/



}
