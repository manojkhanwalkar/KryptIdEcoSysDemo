package util;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class CertUtil {


    private static final String ALGO = "SHA256withECDSA";


/*
    public static PublicKey getPublicKey()
    {
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            FileInputStream is = new FileInputStream("/home/manoj/IdeaProjects/securitytest/src/main/resources/certificate.pem");
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            PublicKey key = cer.getPublicKey();


            //System.out.println(cer);
            return key;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
    */



    public static byte[] extractPublicKeyFromFile(String fileName)
    {
        try {
            File privKeyFile = new File(fileName);
            // read private key DER file
            DataInputStream dis = new DataInputStream(new FileInputStream(privKeyFile));
            byte[] privKeyBytes = new byte[(int)privKeyFile.length()];
            dis.read(privKeyBytes);
            dis.close();

            return privKeyBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PublicKey getPublicKey(byte[] bytes)
    {
        try {

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            X509Certificate cer = (X509Certificate) fact.generateCertificate(bis);
            PublicKey key = cer.getPublicKey();


       //     System.out.println(cer);
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    public static PrivateKey loadPrivateKey(String file){
        try {
            File privKeyFile = new File(file);
            // read private key DER file
            DataInputStream dis = new DataInputStream(new FileInputStream(privKeyFile));
            byte[] privKeyBytes = new byte[(int)privKeyFile.length()];
            dis.read(privKeyBytes);
            dis.close();

            KeyFactory kf = KeyFactory.getInstance("EC");
            // decode private key
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
            PrivateKey privKey = kf.generatePrivate(privSpec);

            return privKey;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }



    public static void main(String[] args) throws Exception {

       PublicKey pub =  getPublicKey(extractPublicKeyFromFile("/home/manoj/IdeaProjects/phoenix/src/main/resources/idpcertificate.pem"));
        PrivateKey priv = loadPrivateKey("/home/manoj/IdeaProjects/phoenix/src/main/resources/idpprivatekey.der");

        String plainText = "Hello World from EC Public and Private keys";

        Signature ecdsaSign = Signature.getInstance(ALGO);
        ecdsaSign.initSign(priv);
        ecdsaSign.update(plainText.getBytes("UTF-8"));
        byte[] signature = ecdsaSign.sign();


        Signature ecdsaVerify = Signature.getInstance(ALGO);

        ecdsaVerify.initVerify(pub);
        ecdsaVerify.update(plainText.getBytes("UTF-8"));
        boolean result = ecdsaVerify.verify(signature);


        System.out.println(result);



    }



}
