package data;

public class EncryptedVerifiedClaim {

    String encryptedKey;

    String verifiedClaim;

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getVerifiedClaim() {
        return verifiedClaim;
    }

    public void setVerifiedClaim(String verifiedClaim) {
        this.verifiedClaim = verifiedClaim;
    }

    @Override
    public String toString() {
        return "EncryptedVerifiedClaim{" +
                "encryptedKey='" + encryptedKey + '\'' +
                ", verifiedClaim=" + verifiedClaim +
                '}';
    }
}
