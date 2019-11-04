package services.INO;

import blockchain.LogManager;
import blockchain.TransactionManager;
import com.codahale.metrics.annotation.Timed;
import constants.Constants;
import data.*;
import org.eclipse.jetty.util.ssl.X509;
import util.*;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static constants.Constants.FileLocation;
import static util.ClaimValidator.*;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class INOResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;

      String certFile = FileLocation+"KryptIdEcoSysDemo/src/main/resources/inocert.pem";
    String privateFile = FileLocation+"KryptIdEcoSysDemo/src/main/resources/inokey.der";

PublicKey publicKey;
PrivateKey privateKey;
    X509Certificate certificate;

    KeyExchangeManager keyExchangeManager;

ClaimManager claimManager ;

VerifiedClaimCache claimCache = new VerifiedClaimCache();


    public INOResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();

       publicKey =  CryptUtil.getPublicKeyFromCertFile(certFile,"EC");
        certificate = CertUtil.getCertificate(certFile);
       privateKey =  CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
       keyExchangeManager = new KeyExchangeManager(publicKey,privateKey,certificate);

       claimManager = new ClaimManager();
    }

    @POST
    @Timed
    @Path("/keyexchange")
    @Produces(MediaType.APPLICATION_JSON)
    public KeyExchange exchange(KeyExchange request) {

       return keyExchangeManager.processExchange(request);

       // return null;

    }


    LogManager logManager = new LogManager();

    @POST
    @Timed
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage verify(EncryptedMessage request) {

       // return IDVerificationManager.getInstance().verify(request);

        keyExchangeManager.verify(request);


        String requestStr = keyExchangeManager.decryptRequest(request);

        VerifyRequest verifyRequest = (VerifyRequest) JSONUtil.fromJSON(requestStr,VerifyRequest.class);
        System.out.println(verifyRequest);

        //logManager.submit(verifyRequest);



        VerifiedClaim claim = claimManager.verifyIdentityAndGenerateClaim(verifyRequest);



        // store in cache for retrieval by subsequent call .

        //TODO - store to dynamodb for later retrieval
        String id = UUID.randomUUID().toString();

        TransactionNumber transactionNumber = new TransactionNumber();
        transactionNumber.setTransactionId(id);

        String transactionNumberStr = JSONUtil.toJSON(transactionNumber);



        X509Certificate rsaCertificate = CertUtil.getCertFromString(verifyRequest.getRsaCert());

        PublicKey rsaPublicKey = rsaCertificate.getPublicKey();

        EncryptedVerifiedClaim encryptedVerifiedClaim = convert(claim,rsaPublicKey);


        claimCache.put(id,encryptedVerifiedClaim);



        EncryptedMessage response =  keyExchangeManager.encryptResponse(transactionNumberStr,request);
        keyExchangeManager.sign(response);
        return response;



    }


    @POST
    @Timed
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage list(EncryptedMessage request) {


        keyExchangeManager.verify(request);
        String requestStr = keyExchangeManager.decryptRequest(request);

        // ignore the request as the function just has to return all claim id's . Get not used here to enable the sender to be verified.

    //    VerifyRequest verifyRequest = (VerifyRequest) JSONUtil.fromJSON(requestStr,VerifyRequest.class);
        System.out.println(requestStr);

        //logManager.submit(verifyRequest);



        List<String> ids = claimCache.getAllClaimIds().stream().map(claimRecord->claimRecord.getClaimId()).collect(Collectors.toList());

        ClaimIds claimIds = new ClaimIds();

        claimIds.setClaimsIds((ArrayList<String>)ids);


        EncryptedMessage response =  keyExchangeManager.encryptResponse(JSONUtil.toJSON(claimIds),request);
        keyExchangeManager.sign(response);
        return response;



    }

    @POST
    @Timed
    @Path("/deleteclaim")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage deleteClaim(EncryptedMessage request) {


        keyExchangeManager.verify(request);
        String claimId = keyExchangeManager.decryptRequest(request);

        System.out.println(claimId);

        claimCache.deleteClaim(claimId);


        EncryptedMessage response =  keyExchangeManager.encryptResponse("Claim deleted",request);
        keyExchangeManager.sign(response);
        return response;



    }




    @POST
    @Timed
    @Path("/claim")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage claim(EncryptedMessage request) {

        // return IDVerificationManager.getInstance().verify(request);

        keyExchangeManager.verify(request);


        String requestStr = keyExchangeManager.decryptRequest(request);

        TransactionNumber transactionNumber = (TransactionNumber) JSONUtil.fromJSON(requestStr,TransactionNumber.class);
        System.out.println(transactionNumber);


         EncryptedVerifiedClaim encryptedVerifiedClaim = claimCache.get(transactionNumber.getTransactionId());

        String responseStr = JSONUtil.toJSON(encryptedVerifiedClaim);
        EncryptedMessage response =  keyExchangeManager.encryptResponse(responseStr,request);
        keyExchangeManager.sign(response);
        return response;



    }







}
