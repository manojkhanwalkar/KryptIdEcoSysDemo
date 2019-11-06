package util;


import cache.SessionKeyCache;
import data.EncryptedMessage;
import data.KeyExchange;
import data.SignatureContainer;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static util.CryptUtil.secureRandom;

public class KeyExchangeManager {



    SessionKeyCache sessionKeyCache = new SessionKeyCache();

    PublicKey publicKey;
    PrivateKey privateKey;
    X509Certificate certificate;

    public KeyExchangeManager(PublicKey publicKey, PrivateKey privateKey, X509Certificate certificate)
    {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.certificate = certificate;
    }


    DHUtil util;


    SignUtil digiSig ;

    SignUtil verifySig ;



    public KeyExchange processExchange(KeyExchange request) {
      //  String responseKey = request.getPublicKey();

        //PublicKey pubKey = CryptUtil.convertStringtoPublicKey(responseKey,"EC");

        String responseCertStr = request.getCertStr();
        PublicKey pubKey = CertUtil.getCertFromString(responseCertStr).getPublicKey();

        util = new DHUtil(publicKey,privateKey);

        util.setReceiverPublicKey(pubKey);


        KeyExchange exchange = new KeyExchange();
      //  String encKey = CryptUtil.convertPublicKeyToString(publicKey);

        String certStr = CertUtil.getCertAsString(certificate);
        exchange.setCertStr(certStr);

       // exchange.setPublicKey(encKey);

        sessionKeyCache.put(request.getClientId(),util);


        digiSig = new SignUtil(publicKey,privateKey,certificate);

        verifySig = new SignUtil(publicKey,privateKey,certificate);
        return exchange;

    }


    public String decryptRequest(EncryptedMessage request)
    {
        DHUtil util = sessionKeyCache.get(request.getClientId());
        return decrypt(request,util);
    }

    public EncryptedMessage encryptResponse(String response, EncryptedMessage request)
    {
        DHUtil util = sessionKeyCache.get(request.getClientId());

        return encrypt(response,util);

    }


    private String decrypt(EncryptedMessage request, DHUtil util)
    {
        return util.decrypt(request.getMessage(),request.getIv());



    }

    private EncryptedMessage encrypt(String str, DHUtil util)
    {
        byte[] iv = new byte[128/8];
        secureRandom.nextBytes(iv);

        String estr = util.encrypt(str,iv);
        EncryptedMessage message = new EncryptedMessage();
        message.setMessage(estr);
        message.setIv(Base64.getEncoder().encodeToString(iv));
        return message;
    }


    public boolean verify(EncryptedMessage encryptedMessage) {
        try {
            boolean verify =  verifySig.verify(encryptedMessage.getContainer(),encryptedMessage.getMessage());

            return true;
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

        return false;


    }


    public void sign(EncryptedMessage message) {


        try {
            SignatureContainer obj = digiSig.sign(message.getMessage());

            message.setContainer(obj);
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
}
