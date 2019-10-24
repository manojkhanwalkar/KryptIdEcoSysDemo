package services.client;

import data.EncryptedMessage;
import data.KeyExchange;
import data.SignatureContainer;
import util.CryptUtil;
import util.DHUtil;
import util.JSONUtil;
import util.SignUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

public class Connection {

    String target;

    HttpClient client ;

    DHUtil util;

    PublicKey publicKey;

    PrivateKey privateKey;

    String clientId = UUID.randomUUID().toString();

    SignUtil digiSig ;

    SignUtil verifySig;


    public Connection(String target, PublicKey publicKey , PrivateKey privateKey)
    {
        this.target = target;
        this.privateKey= privateKey;
        this.publicKey = publicKey;
        final Properties props = System.getProperties();
        props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());

        client = HttpClient.newHttpClient();

        util = new DHUtil(publicKey,privateKey);

         digiSig= new SignUtil(publicKey, privateKey);

        verifySig= new SignUtil(publicKey, privateKey);

    }

    public void connect() throws Exception
    {

            exchangeKeys();

    }


    public String send(String request, String action) throws Exception
    {
        String encMessage = util.encrypt(request);


        EncryptedMessage encryptedMessage = new EncryptedMessage();
        encryptedMessage.setMessage(encMessage);
        encryptedMessage.setClientId(clientId);

        sign(encryptedMessage,encMessage);

        String encryptedResponseStr = post(JSONUtil.toJSON(encryptedMessage),action);

        encryptedMessage = (EncryptedMessage)JSONUtil.fromJSON(encryptedResponseStr,EncryptedMessage.class);

      //  System.out.println("Encrypted claim received " + encryptedMessage.getMessage());

        verify(encryptedMessage);

        String decMessage = util.decrypt(encryptedMessage.getMessage());

        return decMessage;

    }


    private void sign(EncryptedMessage encryptedMessage, String messageToSign)
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


    private void verify(EncryptedMessage encryptedMessage)
    {
        try {
            boolean verify =  verifySig.verify(encryptedMessage.getContainer(),encryptedMessage.getMessage());

            System.out.println("Message signature has been verified " + verify);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }


    private String post(String str , String url) throws Exception
    {

        HttpRequest httprequest = HttpRequest.newBuilder()
                .uri(URI.create(target + url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(str))
                .timeout(Duration.ofMinutes(1))
                .build();


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
        String encKey = CryptUtil.convertPublicKeyToString(publicKey);


        exchange.setPublicKey(encKey);

        exchange.setClientId(clientId);


        String str = JSONUtil.toJSON(exchange);

        String response = post(str,"keyexchange");


        KeyExchange results = (KeyExchange) JSONUtil.fromJSON(response,KeyExchange.class);


        String responseKey = results.getPublicKey();

        PublicKey pubKey = CryptUtil.convertStringtoPublicKey(responseKey,"EC");



        util.setReceiverPublicKey(pubKey);

        System.out.println("Diffie Hellman Elliptic Curve p256v1 Key Hand shake completed ") ;




    }






}
