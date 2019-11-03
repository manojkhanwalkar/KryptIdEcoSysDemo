package services.client;

import constants.Constants;
import data.*;
import org.apache.commons.codec.digest.Crypt;

import services.client.wallet.SimpleWallet;
import services.clientproxy.INOConnector;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static util.ClaimValidator.convert;
import static util.ClaimValidator.isValidClaim;

public class ClientVerifier {





    INOConnector connector =  INOConnector.buildINOConnector();



    public ClientVerifier()
    {


    }







    public TransactionNumber verify(VerifyRequest request) throws Exception
    {

        return connector.verify(request);

    }


    public VerifiedClaim getClaim(TransactionNumber transactionNumber) throws Exception
    {

       return connector.getClaim(transactionNumber);


  /*      Set<AttributeNode> nodes = new HashSet<>();

        AttributeNode attribNode1 = new AttributeNode();
        attribNode1.setName("FirstName");

        AttributeNode attribNode2 = new AttributeNode();
        attribNode2.setName("LastName");


        nodes.addAll(List.of(attribNode1,attribNode2));
        MerkleTree partialTree = MerkleTreeUtil.buildPartial(claim.getTree(),nodes);

        claim.setTree(partialTree); */

        //transact(claim);


    }





    private static VerifyRequest makeSampleRequest()
    {
        VerifyRequest request = new VerifyRequest();

        Identity identity = SampleData.sampleIdentityGenerator();

        request.setIdentity(identity);



        return request;
    }

    public TransactionStatus transact(VerifiedClaim claim) throws Exception
    {

        return connector.transact(claim);
    }

    public ClaimIds getAllClaimIds()
    {
        return connector.getAllClaimIds();
    }


    public TransactionNumber addAttribute(AddAttributeRequest addAttributeRequest)
    {
        /* get existing claim , create Identity object from that , add attributes to the identity object and create new verify request,  and return the claimId of the newly created claim
         */

        return null;
    }







    public static void main(String[] args) throws Exception {



        ClientVerifier verifier = new ClientVerifier();

        ClaimIds claimIds = verifier.getAllClaimIds();

        System.out.println(claimIds);

        Thread.sleep(20000);




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

            AddAttributeRequest addAttributeRequest = new AddAttributeRequest();
            addAttributeRequest.put(Identity.PIIAttributes.FirstName,"Changed");
        addAttributeRequest.put(Identity.PIIAttributes.LastName,"Also Changed");

        System.out.println(JSONUtil.toJSON(addAttributeRequest));

            TransactionNumber transactionNumber;
            VerifyRequest request = makeSampleRequest();

            System.out.println(JSONUtil.toJSON(request));

            transactionNumber = verifier.verify(request);
            VerifiedClaim claim = verifier.getClaim(transactionNumber);

            MerkleTree tree = claim.getTree();

            Identity identity = MerkleTreeUtil.getIdentityAttributes(tree);

            System.out.println(identity);
            
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

            request = new VerifyRequest();
            request.setIdentity(identity);

             transactionNumber = verifier.verify(request);
             VerifiedClaim claim1 = verifier.getClaim(transactionNumber);


            System.out.println(JSONUtil.toPrettyJSON(claim1));

            System.out.println(verifier.transact(claim1));


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
