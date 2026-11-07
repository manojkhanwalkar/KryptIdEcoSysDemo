package services.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import data.*;
import data.Identity;
import util.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

public class Connection {

    String target;

    HttpClient client ;

    DHUtil util;

    PublicKey publicKey;

    PrivateKey privateKey;

    X509Certificate certificate;

    String clientId = UUID.randomUUID().toString();

    SignUtil digiSig ;

    SignUtil verifySig;


    public Connection(String target, PublicKey publicKey , PrivateKey privateKey, X509Certificate certificate)
    {
        this.target = target;
        this.privateKey= privateKey;
        this.publicKey = publicKey;
        this.certificate = certificate;

        initConnectionProps();

        client = HttpClient.newHttpClient();

        util = new DHUtil(publicKey,privateKey);

         digiSig= new SignUtil(publicKey, privateKey,certificate);

        verifySig= new SignUtil(publicKey, privateKey,certificate);

    }



    private void initConnectionProps()
    {
        // Hostname verification stays on by default. Disabling it JVM-wide lets anyone with a certificate
        // trusted by this JVM impersonate any service; allow it only as an explicit, local-dev opt-in.
        if ("true".equalsIgnoreCase(System.getenv("KRYPTID_DISABLE_HOSTNAME_VERIFICATION"))) {
            final Properties props = System.getProperties();
            props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());
        }

    }

    public Connection(String target)
    {
        this.target = target;
        initConnectionProps();
        client = HttpClient.newHttpClient();
    }

    public void connect() throws Exception
    {

            exchangeKeys();

    }

    public String sendSimple(String request,String action) throws Exception
    {
        return post(request,action);
    }

    static SecureRandom secureRandom = new SecureRandom();


    public String send(String request, String action) throws Exception
    {
        byte[] iv = new byte[128/8];
        secureRandom.nextBytes(iv);

        String encMessage = util.encrypt(request,iv);


        EncryptedMessage encryptedMessage = new EncryptedMessage();
        encryptedMessage.setMessage(encMessage);
        encryptedMessage.setClientId(clientId);
        encryptedMessage.setIv(Base64.getEncoder().encodeToString(iv));

        sign(encryptedMessage,encMessage);

        String encryptedResponseStr = post(JSONUtil.toJSON(encryptedMessage),action);

        encryptedMessage = (EncryptedMessage)JSONUtil.fromJSON(encryptedResponseStr,EncryptedMessage.class);

      //  System.out.println("Encrypted claim received " + encryptedMessage.getMessage());

        verify(encryptedMessage);

        String decMessage = util.decrypt(encryptedMessage.getMessage(),encryptedMessage.getIv());

        return decMessage;

    }


    private void sign(JavaWebEncryptedMessage encryptedMessage, String messageToSign)
    {
        try {
            SignatureContainer obj = digiSig.sign(messageToSign);

            encryptedMessage.setContainer(obj);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }


    private void sign(EncryptedMessage encryptedMessage, String messageToSign)
    {
        try {
            SignatureContainer obj = digiSig.sign(messageToSign);
            JavaWebSignatureContainer obj1 = digiSig.jwsSign(messageToSign);

            encryptedMessage.setContainer(obj);
            encryptedMessage.setJwsContainer(obj1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }


    /** Base64 certificate the server presented during key exchange; responses must be signed by it. */
    String serverCertStr;

    private void requireServerCert(String signerCertStr)
    {
        if (serverCertStr == null || signerCertStr == null
                || !MessageDigest.isEqual(serverCertStr.getBytes(), signerCertStr.getBytes()))
            throw new SecurityException("Response not signed by the server certificate from the key exchange");
    }

    private void verify(JavaWebEncryptedMessage encryptedMessage)
    {
        if (encryptedMessage == null || encryptedMessage.getContainer() == null || encryptedMessage.getMessage() == null)
            throw new SecurityException("Unsigned response");
        requireServerCert(encryptedMessage.getContainer().getCertStr());
        boolean valid;
        try {
            valid = verifySig.verify(encryptedMessage.getContainer(),encryptedMessage.getMessage());
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            valid = false;
        }
        if (!valid)
            throw new SecurityException("Invalid response signature");
    }


    private void verify(EncryptedMessage encryptedMessage)
    {
        if (encryptedMessage == null || encryptedMessage.getContainer() == null
                || encryptedMessage.getJwsContainer() == null || encryptedMessage.getMessage() == null)
            throw new SecurityException("Unsigned response");
        requireServerCert(encryptedMessage.getContainer().getCertStr());
        requireServerCert(encryptedMessage.getJwsContainer().getCertStr());
        boolean valid;
        try {
            valid = verifySig.verify(encryptedMessage.getContainer(),encryptedMessage.getMessage())
                    && verifySig.verifyJWS(encryptedMessage.getJwsContainer(), encryptedMessage.getMessage());
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            valid = false;
        }
        if (!valid)
            throw new SecurityException("Invalid response signature");
    }

    private String post(String str , String url) throws Exception
    {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(target + url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(str))
                .timeout(Duration.ofMinutes(1));

        // shared key comes from KRYPTID_HMAC_KEY; services that require the header reject requests without it
        String hmacKey = HMACUtil.configuredKey();
        if (hmacKey != null)
            builder.header("HMAC" , HMACUtil.calculateHMAC(str,hmacKey));

        HttpRequest httprequest = builder.build();


        HttpResponse<String> response =
                client.send(httprequest, HttpResponse.BodyHandlers.ofString());

        return response.body();


    }



    public String get( String url) throws Exception
    {

        HttpRequest httprequest = HttpRequest.newBuilder()
                .uri(URI.create(target + url))
                .GET()
                .timeout(Duration.ofMinutes(1))
                .build();


        HttpResponse<String> response =
                client.send(httprequest, HttpResponse.BodyHandlers.ofString());

        return response.body();


    }





    private  void exchangeKeys() throws Exception {


        KeyExchange exchange = new KeyExchange();
     //   String encKey = CryptUtil.convertPublicKeyToString(publicKey);

        String certStr = CertUtil.getCertAsString(certificate);


       // exchange.setPublicKey(encKey);

        exchange.setCertStr(certStr);

        exchange.setClientId(clientId);


        String str = JSONUtil.toJSON(exchange);

        String response = post(str,"keyexchange");


        KeyExchange results = (KeyExchange) JSONUtil.fromJSON(response,KeyExchange.class);

        if (results == null || results.getCertStr() == null)
            throw new SecurityException("Invalid key exchange response");
        String responseCertStr = results.getCertStr();
        serverCertStr = responseCertStr;

        PublicKey pubKey = CertUtil.getCertFromString(responseCertStr).getPublicKey();


//        String responseKey = results.getPublicKey();

  //      PublicKey pubKey = CryptUtil.convertStringtoPublicKey(responseKey,"EC");



        util.setReceiverPublicKey(pubKey);

        System.out.println("Diffie Hellman Elliptic Curve p256v1 Key Hand shake completed ") ;




    }


    public void sendCBOR(String url) {

        try {
            CBORFactory f = new CBORFactory();
            ObjectMapper mapper = new ObjectMapper(f);

            Identity identity = new Identity();
            identity.setEmail("test@test.com");
            identity.setAddress("Hello CBOR Street");
            byte[] cborData = mapper.writeValueAsBytes(identity);


            HttpRequest httprequest = HttpRequest.newBuilder()
                    .uri(URI.create(target + url))
                    .header("Content-Type", "*/*")

                    .POST(HttpRequest.BodyPublishers.ofByteArray(cborData))
                    .timeout(Duration.ofMinutes(1))
                    .build();

            HttpResponse<byte[]> response =
                    client.send(httprequest, HttpResponse.BodyHandlers.ofByteArray());


            Nonces nonces = mapper.readValue(response.body(), Nonces.class);

            System.out.println(nonces);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void sendJWE() {

        String request = "Dummy Payload";

        String encMessage = util.encryptJWE(request);


        JavaWebEncryptedMessage encryptedMessage = new JavaWebEncryptedMessage();
        encryptedMessage.setMessage(encMessage);
        encryptedMessage.setClientId(clientId);

        sign(encryptedMessage,encMessage);

        String encryptedResponseStr = null;
        try {
            encryptedResponseStr = post(JSONUtil.toJSON(encryptedMessage),"nonce2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        encryptedMessage = (JavaWebEncryptedMessage)JSONUtil.fromJSON(encryptedResponseStr,JavaWebEncryptedMessage.class);

        //  System.out.println("Encrypted claim received " + encryptedMessage.getMessage());

        verify(encryptedMessage);

        String decMessage = util.decryptJWE(encryptedMessage.getMessage());


        System.out.println(decMessage);




    }
}
