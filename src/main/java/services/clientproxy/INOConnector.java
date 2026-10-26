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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

            // INO enforces single-use nonces on /verify; fetch one if the caller did not supply it
            if (request.getNonce() == null) {
                Nonces nonces = getNonces();
                if (nonces == null || nonces.getNonces() == null || nonces.getNonces().isEmpty())
                    throw new IllegalStateException("Could not obtain a nonce from INO");
                request.setNonce(nonces.getNonces().get(0));
            }

            String str = JSONUtil.toJSON(request);


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



        String response = ino.send(str,"claim");

        return getClaim(response,wallet.getRsaPrivateKey());

    }

    public VerifiedClaim getClaim(ClaimInfo claimInfo)
    {

        SimpleWallet wallet = SimpleWallet.getWallet(claimInfo.getClaimId());

        String response = claimInfo.getEncryptedVerifiedClaimStr();

        return getClaim(response,wallet.getRsaPrivateKey());


    }

    private VerifiedClaim getClaim(String response, PrivateKey privateKey)
    {
        EncryptedVerifiedClaim encryptedVerifiedClaim = (EncryptedVerifiedClaim) JSONUtil.fromJSON(response,EncryptedVerifiedClaim.class);



        VerifiedClaim claim = convert(encryptedVerifiedClaim,privateKey);


        if (!isValidClaim(claim))
            throw new SecurityException("Received claim failed validation");






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


    public VerifyRequest createNewRequest(AddAttributeRequest addAttributeRequest, VerifiedClaim claim) {

        MerkleTree tree = claim.getTree();

        Identity identity = MerkleTreeUtil.getIdentityAttributes(tree);


        // iterate over add attribute request and set it on identity

        addAttributeRequest.getAttributes().entrySet().stream().forEach((entry)-> {

            switch(entry.getKey())
            {
                case FirstName:
                    identity.setFirstName(entry.getValue());
                    break;
                case LastName:
                    identity.setLastName(entry.getValue());
                    break;
                case Zip:
                    identity.setZip(entry.getValue());
                    break;
                case Address:
                    identity.setAddress(entry.getValue());
                    break;
                case SSN:
                    identity.setSsn(entry.getValue());
                    break;
                case Email:
                    identity.setEmail(entry.getValue());
                    break;
                case BirthDate:
                    identity.setBirthDate(LocalDate.parse(entry.getValue()));
                    break;

                default :
                    System.out.println("Erroneous attribute " + entry);

            }

        });

        // identity.setFirstName("Changed");

        VerifyRequest request = new VerifyRequest();
        request.setIdentity(identity);

        return request;


    }

    public ClaimIds getAllClaimIds() {


        try {


            String str = JSONUtil.toJSON("Dummy request string which will be ignored");

            String response = ino.send(str,"list");

            ClaimIds claimIds= (ClaimIds) JSONUtil.fromJSON(response,ClaimIds.class);



            return claimIds;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;



    }


    public void deleteClaim(String claimId) {


        try {

            ino.send(claimId,"deleteclaim");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void deleteClaims() {


        try {

            ino.send("dummy string ","deleteclaims");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<VerifiedClaim> getAllClaims() {
        try {
            String response = ino.send("dummystring" , "claims");
            Claims claims= (Claims) JSONUtil.fromJSON(response,Claims.class);
            List<VerifiedClaim> verifiedClaims = claims.getClaims().stream().map(this::getClaim).collect(Collectors.toList());

            return verifiedClaims;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Nonces getNonces() {

        try {
            String response = ino.send("dummystring" , "nonce");
            Nonces nonces = (Nonces) JSONUtil.fromJSON(response,Nonces.class);
            return nonces;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void getCBORNonce() {

        ino.sendCBOR("nonce1");

    }

    public void getNonceUsingJWE() {
        ino.sendJWE();
    }
}
