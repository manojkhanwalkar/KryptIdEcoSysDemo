package util;

import data.JavaWebSignatureContainer;
import data.SignatureContainer;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;



// using the same keys for signing and key exchange - in practice a separate set of keys will be used.


public class SignUtil {

    private static final String SPEC = "secp256k1";
    private static final String ALGO = "SHA256withECDSA";
  //  private final String certificateFile;
  //  private final String privateKeyFile;

    private final PrivateKey privateKey;

    //private final String publicKeyStr ;

    private final String certStr ;




    public SignUtil(PublicKey publicKey, PrivateKey  privateKey, X509Certificate certificate)
    {

        this.privateKey = privateKey;
       // publicKeyStr= CryptUtil.convertPublicKeyToString(publicKey);

        this.certStr = CertUtil.getCertAsString(certificate);

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
     //   signatureContainer.setPublicKey(publicKeyStr);
        signatureContainer.setCertStr(certStr);
        signatureContainer.setSignature(sig);


        return signatureContainer;
    }

    public JavaWebSignatureContainer jwsSign(String messageToSign) {
        JavaWebSignatureContainer signatureContainer = new JavaWebSignatureContainer();
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(messageToSign);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
        jws.setKey(privateKey);
        String jwsCompactSerialization = null;
        try {
            jwsCompactSerialization = jws.getCompactSerialization();
        } catch (JoseException e) {
            e.printStackTrace();
        }
        signatureContainer.setCertStr(certStr);
        signatureContainer.setSignature(jwsCompactSerialization);

        return signatureContainer;

    }


    public synchronized boolean  verify(SignatureContainer signatureContainer, String message) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException, SignatureException {

       Signature ecdsaVerify = Signature.getInstance(signatureContainer.getAlgorithm());

     /*   KeyFactory kf = KeyFactory.getInstance("EC");

        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(signatureContainer.getPublicKey()));

       // KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec); */

    //    PublicKey publicKey = CryptUtil.convertStringtoPublicKey(signatureContainer.getPublicKey(),"EC");

        PublicKey publicKey = CertUtil.getCertFromString(signatureContainer.getCertStr()).getPublicKey();

        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(message.getBytes("UTF-8"));
        boolean result = ecdsaVerify.verify(Base64.getDecoder().decode(signatureContainer.getSignature()));
        return result;
    }


    public boolean verifyJWS(JavaWebSignatureContainer jwsContainer, String message) {
        PublicKey publicKey = CertUtil.getCertFromString(jwsContainer.getCertStr()).getPublicKey();

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmConstraints(new org.jose4j.jwa.AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256));
        boolean signatureVerified = false;
        try {
            jws.setCompactSerialization(message);
            jws.setKey(publicKey);
            signatureVerified = jws.verifySignature();
        } catch (JoseException e) {
            e.printStackTrace();
        }
        System.out.println("JWS Signature is valid: " + signatureVerified);

        return signatureVerified;

      /*  String payload = jws.getPayload();
        System.out.println("JWS payload: " + payload); */




    }
}



