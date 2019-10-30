package data;

public class DoubleEncryptedVerifiedClaim {

    String encryptedKey;

    String encryptedVerifiedClaim;

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getEncryptedVerifiedClaim() {
        return encryptedVerifiedClaim;
    }

    public void setEncryptedVerifiedClaim(String verifiedClaim) {
        this.encryptedVerifiedClaim = verifiedClaim;
    }

    @Override
    public String toString() {
        return "EncryptedVerifiedClaim{" +
                "encryptedKey='" + encryptedKey + '\'' +
                ", verifiedClaim=" + encryptedVerifiedClaim +
                '}';
    }
}
