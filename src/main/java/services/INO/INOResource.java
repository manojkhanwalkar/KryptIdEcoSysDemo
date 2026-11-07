package services.INO;

import blockchain.LogManager;
import blockchain.TransactionManager;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import constants.Constants;
import data.*;
import org.eclipse.jetty.util.ssl.X509;
import util.*;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

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

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(INOResource.class.getName());

    // Shared HMAC key from KRYPTID_HMAC_KEY (see HMACUtil.configuredKey); never hard-coded.
    private void validateHMAC(String request, HttpHeaders headers)
    {
        String key = HMACUtil.configuredKey();
        if (key == null) {
            LOG.severe("KRYPTID_HMAC_KEY is not configured (min 32 chars); rejecting HMAC-protected request");
            throw new WebApplicationException("HMAC key not configured", Response.Status.SERVICE_UNAVAILABLE);
        }

        java.util.List<String> values = headers == null ? null : headers.getRequestHeader("HMAC");
        String value = (values == null || values.isEmpty()) ? null : values.get(0);

        if (!HMACUtil.isValid(value, request, key)) {
            LOG.warning("HMAC validation failed for /verify request");
            throw new WebApplicationException("Invalid HMAC", Response.Status.UNAUTHORIZED);
        }
    }

    @POST
    @Timed
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage verify(EncryptedMessage request, @Context HttpHeaders httpHeaders) {

        validateHMAC(JSONUtil.toJSON(request),httpHeaders);

       // return IDVerificationManager.getInstance().verify(request);

        keyExchangeManager.verify(request);


        String requestStr = keyExchangeManager.decryptRequest(request);

        VerifyRequest verifyRequest = (VerifyRequest) JSONUtil.fromJSON(requestStr,VerifyRequest.class);
        if (verifyRequest == null || verifyRequest.getIdentity() == null || verifyRequest.getRsaCert() == null)
            throw new WebApplicationException("Malformed verify request", Response.Status.BAD_REQUEST);

        // replay protection: each verify request must carry a fresh, single-use nonce issued by /nonce
        boolean nonceVerified = verifyRequest.getNonce() != null && verifyRequest.getNonce().getNonce() != null
                && claimCache.verifyNonce(verifyRequest.getNonce());
        if (!nonceVerified) {
            LOG.warning("Rejected verify request with missing, unknown or reused nonce");
            throw new WebApplicationException("Invalid nonce", Response.Status.UNAUTHORIZED);
        }

        //logManager.submit(verifyRequest);



        VerifiedClaim claim = claimManager.verifyIdentityAndGenerateClaim(verifyRequest);



        // store in cache for retrieval by subsequent call .

        //TODO - store to dynamodb for later retrieval
        String id = UUID.randomUUID().toString();

        TransactionNumber transactionNumber = new TransactionNumber();
        transactionNumber.setTransactionId(id);

        String transactionNumberStr = JSONUtil.toJSON(transactionNumber);



        if (claim == null)
            throw new WebApplicationException("Identity verification failed", Response.Status.INTERNAL_SERVER_ERROR);

        X509Certificate rsaCertificate = CertUtil.getCertFromString(verifyRequest.getRsaCert());
        if (rsaCertificate == null)
            throw new WebApplicationException("Invalid RSA certificate", Response.Status.BAD_REQUEST);

        PublicKey rsaPublicKey = rsaCertificate.getPublicKey();

        EncryptedVerifiedClaim encryptedVerifiedClaim = convert(claim,rsaPublicKey);


        claimCache.put(id,encryptedVerifiedClaim);



        EncryptedMessage response =  keyExchangeManager.encryptResponse(transactionNumberStr,request);
        keyExchangeManager.sign(response);
        return response;



    }


    @POST
    @Timed
    @Path("/nonce")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage nonce(EncryptedMessage request) {


        keyExchangeManager.verify(request);
        String requestStr = keyExchangeManager.decryptRequest(request);

        // ignore the request as the function just has to return all claim id's . Get not used here to enable the sender to be verified.

        //    VerifyRequest verifyRequest = (VerifyRequest) JSONUtil.fromJSON(requestStr,VerifyRequest.class);

        //logManager.submit(verifyRequest);

        Nonces nonces = claimCache.getNonces();



        EncryptedMessage response =  keyExchangeManager.encryptResponse(JSONUtil.toJSON(nonces),request);
        keyExchangeManager.sign(response);
        return response;



    }


    @POST
    @Timed
    @Path("/nonce2")
    @Produces(MediaType.APPLICATION_JSON)
    public JavaWebEncryptedMessage nonce2(JavaWebEncryptedMessage request) {


        keyExchangeManager.verify(request);

        String requestStr = keyExchangeManager.decryptRequest(request);

        // ignore the request as the function just has to return all claim id's . Get not used here to enable the sender to be verified.

        //    VerifyRequest verifyRequest = (VerifyRequest) JSONUtil.fromJSON(requestStr,VerifyRequest.class);

        //logManager.submit(verifyRequest);

        Nonces nonces = claimCache.getNonces();



        JavaWebEncryptedMessage response =  keyExchangeManager.encryptResponse(JSONUtil.toJSON(nonces),request);
        keyExchangeManager.sign(response);
        return response;



    }



    @POST
    @Timed
    @Path("/nonce1")
    @Produces(MediaType.WILDCARD)
    public byte[] nonce1(byte[] bytes) {


        try {
            Nonces nonces = claimCache.getNonces();

            CBORFactory f = new CBORFactory();
            ObjectMapper mapper = new ObjectMapper(f);

            Identity otherValue = mapper.readValue(bytes, Identity.class);



            byte[] cborData = mapper.writeValueAsBytes(nonces);

            return cborData;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;


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


        claimCache.deleteClaim(claimId);


        EncryptedMessage response =  keyExchangeManager.encryptResponse("Claim deleted",request);
        keyExchangeManager.sign(response);
        return response;



    }

    @POST
    @Timed
    @Path("/deleteclaims")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage deleteAllClaims(EncryptedMessage request) {


        keyExchangeManager.verify(request);


        claimCache.deleteClaims();


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


         EncryptedVerifiedClaim encryptedVerifiedClaim = claimCache.get(transactionNumber.getTransactionId());

        String responseStr = JSONUtil.toJSON(encryptedVerifiedClaim);
        EncryptedMessage response =  keyExchangeManager.encryptResponse(responseStr,request);
        keyExchangeManager.sign(response);
        return response;



    }

    @POST
    @Timed
    @Path("/claims")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage claims(EncryptedMessage request) {

        // return IDVerificationManager.getInstance().verify(request);

        keyExchangeManager.verify(request);




        Claims claims = claimCache.getAllClaims();

        String responseStr = JSONUtil.toJSON(claims);
        EncryptedMessage response =  keyExchangeManager.encryptResponse(responseStr,request);
        keyExchangeManager.sign(response);
        return response;



    }








}
