package services.SP;

import com.codahale.metrics.annotation.Timed;
import constants.Constants;
import data.*;
import services.client.Connection;
import services.client.DiscoveryClient;
import util.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicLong;

import static util.ClaimValidator.convert;
import static util.ClaimValidator.isValidClaim;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SPResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;


    private String certFile = Constants.FileLocation+"KryptIdEcoSysDemo/src/main/resources/spcert.pem";
    private String privateFile = Constants.FileLocation+"KryptIdEcoSysDemo/src/main/resources/spkey.der";

    private String rsaCertFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/sprsacertificate.pem";
    private String rsaPrivateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/sprsakey.der";


 //   String bureauURL = "https://172.17.0.1:8480/";

    private PublicKey publicKey;
    private PrivateKey privateKey;
    X509Certificate certificate;

    private KeyExchangeManager keyExchangeManager;

    private  Connection bureau;


    private RSAKeyHolder rsaKeyHolder;


    public SPResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();

        publicKey =  CryptUtil.getPublicKeyFromCertFile(certFile,"EC");

        privateKey =  CryptUtil.getPrivateKeyDerFromFile(privateFile,"EC");
        certificate = CertUtil.getCertificate(certFile);
        keyExchangeManager = new KeyExchangeManager(publicKey,privateKey,certificate);

        rsaKeyHolder = new RSAKeyHolder(rsaCertFile,rsaPrivateFile);

      /*  bureau = new Connection(bureauURL,publicKey,privateKey);
        try {
            bureau.connect();
        } catch (Exception e) {
            e.printStackTrace();
        } */
    }

    @POST
    @Timed
    @Path("/keyexchange")
    @Produces(MediaType.APPLICATION_JSON)
    public KeyExchange exchange(KeyExchange request) {

        return keyExchangeManager.processExchange(request);


    }


    @GET
    @Timed
    @Path("/claimkey")
    @Produces(MediaType.APPLICATION_JSON)
    public String exchange() {


            return rsaKeyHolder.getPublicKeyStr();

    }


    DiscoveryClient discoveryClient = new DiscoveryClient(Constants.discoveryServiceUrl);



    @POST
    @Timed
    @Path("/transact")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedMessage verify(EncryptedMessage request) {

        keyExchangeManager.verify(request);

        String requestStr = keyExchangeManager.decryptRequest(request);

        // Encrypted Verified claim to be processed here .
        EncryptedVerifiedClaim encryptedVerifiedClaim = (EncryptedVerifiedClaim) JSONUtil.fromJSON(requestStr,EncryptedVerifiedClaim.class);

        VerifiedClaim claim = convert(encryptedVerifiedClaim,rsaKeyHolder.getPrivateKey());

        isValidClaim(claim);

        System.out.println(claim);

      //  requestStr = JSONUtil.toJSON(claim);



        String bureauResponse = null;
        try {


            discoveryClient = new DiscoveryClient(Constants.discoveryServiceUrl);

            String bureauURL = discoveryClient.service(Constants.BureauServiceType).getServiceUrl();



            bureau = new Connection(bureauURL,publicKey,privateKey,certificate);

            bureau.connect();

            String spPublicKey = bureau.get("claimkey");

            System.out.println("Got public key from service provider " + spPublicKey);

             encryptedVerifiedClaim = convert(claim,spPublicKey);

            requestStr = JSONUtil.toJSON(encryptedVerifiedClaim);



            bureauResponse = bureau.send(requestStr,"report");
        } catch (Exception e) {
            e.printStackTrace();
        }

        CreditScore score = (CreditScore) JSONUtil.fromJSON(bureauResponse,CreditScore.class);

        TransactionStatus transactionStatus = new TransactionStatus();

        if (score.getScore() > 600)
        {
            transactionStatus.setStatus(TransactionStatus.Status.Approved);
        }
        else
        {
            transactionStatus.setStatus(TransactionStatus.Status.Rejected);
        }


        System.out.println(score);

        String responseStr = JSONUtil.toJSON(transactionStatus);
        EncryptedMessage response =  keyExchangeManager.encryptResponse(responseStr,request);
        keyExchangeManager.sign(response);
        return response;

    }









}
