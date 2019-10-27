package util;




import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;


public class DHUtil {

    private PublicKey publickey;
    private PublicKey otherPublicKey;
    PrivateKey privateKey;
    KeyAgreement keyAgreement;
    byte[] sharedsecret;

    String ALGO = "AES";

    //CertUtil  util = new CertUtil();


    public DHUtil(PublicKey publicKey , PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.publickey = publicKey;

        makeKeyExchangeParams();
    }


    private void makeKeyExchangeParams() {
        try {

            keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(privateKey);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    Key sessionKey;

    public void setReceiverPublicKey(PublicKey publickey) {
        try {
            keyAgreement.doPhase(publickey, true);
            sharedsecret = keyAgreement.generateSecret();
            otherPublicKey = publickey;

            sessionKey = generateKey();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }


    public  String encrypt(String msg) {
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, sessionKey);
            byte[] encVal = c.doFinal(msg.getBytes());
            return new String(Base64.getEncoder().encode(encVal));
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public  String decrypt(String encryptedData) {
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, sessionKey);
            byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
            byte[] decValue = c.doFinal(decordedValue);
            return new String(decValue);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encryptedData;
    }

    public PublicKey getPublickey() {
        return publickey;
    }

    protected Key generateKey() {


//        return new SecretKeySpec(sharedsecret, ALGO);

          return new SecretKeySpec(derivKey(), ALGO);

    }


    // message digest step to shared secret to ensure multiple keys dont derive to the same shared secret .

    byte[] derivKey() {
        try {
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            hash.update(sharedsecret);
            // Simple deterministic ordering
            List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(publickey.getEncoded()), ByteBuffer.wrap(otherPublicKey.getEncoded()));
            Collections.sort(keys);
            hash.update(keys.get(0));
            hash.update(keys.get(1));

            byte[] derivedKey = hash.digest();

            return derivedKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }







}