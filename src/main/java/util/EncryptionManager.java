package util;


import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class EncryptionManager {


  /*  String certFile = "/home/manoj/IdeaProjects/KryptId/src/main/resources/ubercertificate.pem";
    String privateFile = "/home/manoj/IdeaProjects/KryptId/src/main/resources/uberprivatekey.der";


    DHUtil util = new DHUtil(privateFile,certFile);
    SignUtil digiSig = new SignUtil(certFile,privateFile);

    SignUtil verifySig = new SignUtil(certFile,privateFile);



    public String decrypt(EncryptedMessage request)
    {
        String str = util.decrypt(request.getMessage());

        return str;

    }

    public EncryptedMessage encrypt(String str)
    {
        String estr = util.encrypt(str);
        EncryptedMessage message = new EncryptedMessage();
        message.setMessage(estr);
        return message;
    }




    public KeyExchange processExchange(KeyExchange request) {
        byte[] responseKey = request.getPublicKeyCertificate();

        PublicKey pubKey = CertUtil.getPublicKey(responseKey);

        util = new DHUtil(privateFile,certFile);

        util.setReceiverPublicKey(pubKey);


        KeyExchange exchange = new KeyExchange();
        byte[] encKey = CertUtil.extractPublicKeyFromFile(certFile);


        exchange.setPublicKeyCertificate(encKey);

        return exchange;

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


    } */
}
