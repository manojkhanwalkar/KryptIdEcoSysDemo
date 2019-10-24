package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerifyRequest {


    Identity identity;

    @Override
    public String toString() {
        return "VerifyRequest{" +
                "identity=" + identity +
                ", rsaPublicKey='" + rsaPublicKey + '\'' +
                '}';
    }

    @JsonProperty
    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    String rsaPublicKey;

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }
}
