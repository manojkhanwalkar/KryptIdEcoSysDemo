package util;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Base64;


// using the same keys for signing and key exchange - in practice a separate set of keys will be used.


public class VerifiedClaimSignUtil {

    private static final String SPEC = "secp256k1";
    private static final String ALGO = "SHA256withECDSA";
  //  private final String certificateFile;
    //private final String privateKeyFile;

    private final PrivateKey privateKey;

    private final PublicKey publicKey ;


    public VerifiedClaimSignUtil(PublicKey publicKey , PrivateKey privateKey)
    {
        this.publicKey = publicKey;
        this.privateKey = privateKey;

    }

    public VerifiedClaimSignUtil(String publicKeyStr)
    {
        this.publicKey = CryptUtil.convertStringtoPublicKey(publicKeyStr,"EC");

        privateKey=null;
    }


public PublicKey getPublicKey()
{
    return publicKey;
}


    public synchronized String sign(String plainText)  {

        try {
            Signature ecdsaSign = Signature.getInstance(ALGO);
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(plainText.getBytes("UTF-8"));
            byte[] signature = ecdsaSign.sign();
            String sig = Base64.getEncoder().encodeToString(signature);

            return sig;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
            return null;

    }

    public synchronized boolean  verify( String signature , String message)  {

        try {
            Signature ecdsaVerify = Signature.getInstance(ALGO);



            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(message.getBytes("UTF-8"));
            boolean result = ecdsaVerify.verify(Base64.getDecoder().decode(signature));
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return false;
    }






}



