package util;

import data.SignatureContainer;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;



// using the same keys for signing and key exchange - in practice a separate set of keys will be used.


public class SignUtil {

    private static final String SPEC = "secp256k1";
    private static final String ALGO = "SHA256withECDSA";
  //  private final String certificateFile;
  //  private final String privateKeyFile;

    private final PrivateKey privateKey;

    private final String publicKeyStr ;


    public SignUtil(PublicKey publicKey, PrivateKey  privateKey)
    {

        this.privateKey = privateKey;
        publicKeyStr= CryptUtil.convertPublicKeyToString(publicKey);


    }

    public synchronized SignatureContainer sign(String plainText) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, SignatureException {

  /*      ECGenParameterSpec ecSpec = new ECGenParameterSpec(SPEC);
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        g.initialize(ecSpec, new SecureRandom());
        KeyPair keypair = g.generateKeyPair();
        PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate(); */

      //  PublicKey publicKey =  getPublicKey(extractPublicKeyFromFile(certificateFile));
      //  PrivateKey



        //...... sign
        Signature ecdsaSign = Signature.getInstance(ALGO);
        ecdsaSign.initSign(privateKey);
        ecdsaSign.update(plainText.getBytes("UTF-8"));
        byte[] signature = ecdsaSign.sign();
        String sig = Base64.getEncoder().encodeToString(signature);

        SignatureContainer signatureContainer = new SignatureContainer();
        signatureContainer.setAlgorithm(ALGO);
        signatureContainer.setPublicKey(publicKeyStr);
        signatureContainer.setSignature(sig);


        return signatureContainer;
    }

    public synchronized boolean  verify(SignatureContainer signatureContainer, String message) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException, SignatureException {

       Signature ecdsaVerify = Signature.getInstance(signatureContainer.getAlgorithm());

     /*   KeyFactory kf = KeyFactory.getInstance("EC");

        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(signatureContainer.getPublicKey()));

       // KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec); */

        PublicKey publicKey = CryptUtil.convertStringtoPublicKey(signatureContainer.getPublicKey(),"EC");

        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(message.getBytes("UTF-8"));
        boolean result = ecdsaVerify.verify(Base64.getDecoder().decode(signatureContainer.getSignature()));
        return result;
    }







}



