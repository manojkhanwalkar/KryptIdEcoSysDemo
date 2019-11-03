package services.clientproxy;

import com.codahale.metrics.annotation.Timed;
import data.*;
import services.client.wallet.SimpleWallet;
import util.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static util.ClaimValidator.convert;
import static util.ClaimValidator.isValidClaim;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ClientProxyResource {
    private final String template;
    private final String defaultName;




    public ClientProxyResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
    }




    @POST
    @Timed
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public String update(AddAttributeRequest addAttributeRequest) {

        try {
            INOConnector connector =  INOConnector.buildINOConnector();
            TransactionNumber transactionNumber = new TransactionNumber();

            transactionNumber.setTransactionId(addAttributeRequest.getClaimId());
            VerifiedClaim claim=  connector.getClaim(transactionNumber);

            VerifyRequest verifyRequest = connector.createNewRequest(addAttributeRequest,claim);

             transactionNumber = connector.verify(verifyRequest);
            return transactionNumber.getTransactionId();
        } catch (Exception e) {
            e.printStackTrace();
        }



        return "Update failed " ;



    }

//   {"claimId":"0d9e5eb0-6002-40e0-b7c9-789d19456f0e","attributes":{"LastName":"Also Changed","FirstName":"Changed"}}



    @POST
    @Timed
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public String verify(VerifyRequest verifyRequest) {


        INOConnector connector =  INOConnector.buildINOConnector();

      //  VerifyRequest verifyRequest = INOConnector.makeSampleRequest();
        TransactionNumber transactionNumber = connector.verify(verifyRequest);
        return transactionNumber.getTransactionId();


    }

    @POST
    @Timed
    @Path("/claim")
    @Produces(MediaType.APPLICATION_JSON)
    public VerifiedClaim claim(String transactionId) {

        try {
            INOConnector connector =  INOConnector.buildINOConnector();
            TransactionNumber transactionNumber = new TransactionNumber();

            transactionNumber.setTransactionId(transactionId);
            return connector.getClaim(transactionNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

            return null;
    }



    @POST
    @Timed
    @Path("/transact")
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionStatus transact(String transactionId) {

        try {
            INOConnector connector =  INOConnector.buildINOConnector();
            TransactionNumber transactionNumber = new TransactionNumber();

            transactionNumber.setTransactionId(transactionId);
            VerifiedClaim verifiedClaim = connector.getClaim(transactionNumber);
            return connector.transact(verifiedClaim);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;


    }





}
