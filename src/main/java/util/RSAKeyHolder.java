package util;

import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAKeyHolder {

   final  PublicKey publicKey;
    final PrivateKey privateKey;
    final String publicKeyStr ;
    final String privateKeyStr;

    public RSAKeyHolder(String certFile , String privateFile)
    {
        publicKey = CryptUtil.getPublicKeyFromCertFile(certFile,"RSA");
        privateKey = CryptUtil.getPrivateKeyDerFromFile(privateFile,"RSA");
        publicKeyStr = CryptUtil.convertPublicKeyToString(publicKey);
        privateKeyStr = CryptUtil.convertPrivateKeyToString(privateKey);

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


    public static void main(String[] args) {

        String rsaCertFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/sprsacertificate.pem";
        String rsaPrivateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/sprsakey.der";

        RSAKeyHolder    rsaKeyHolder = new RSAKeyHolder(rsaCertFile,rsaPrivateFile);

    }
}
