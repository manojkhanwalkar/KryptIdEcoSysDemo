package util;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class RSAKeyHolder {

   final  PublicKey publicKey;
    final PrivateKey privateKey;
    final String publicKeyStr ;
    final String privateKeyStr;
    final X509Certificate certificate;
    final String certString;

    public RSAKeyHolder(String certFile , String privateFile)
    {
        publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"RSA");
        privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"RSA");
        publicKeyStr = CryptUtil.convertPublicKeyToString(publicKey);
        privateKeyStr = CryptUtil.convertPrivateKeyToString(privateKey);
        certificate = CertUtil.getCertificate(certFile);
        certString = CertUtil.getCertAsString(certificate);

    }

    public String getCertString() {
        return certString;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getPublicKeyStr() {
        return publicKeyStr;
    }

    public String getPrivateKeyStr() {
        return privateKeyStr;
    }



}
