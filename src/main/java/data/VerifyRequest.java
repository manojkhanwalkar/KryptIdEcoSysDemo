package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerifyRequest {


    Identity identity;

    @Override
    public String toString() {
        return "VerifyRequest{" +
                "identity=" + identity +
                ", rsaCert='" + rsaCert + '\'' +
                '}';
    }

    @JsonProperty
    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    String rsaCert;

    public String getRsaCert() {
        return rsaCert;
    }

    public void setRsaCert(String rsaCert) {
        this.rsaCert = rsaCert;
    }

    Nonce nonce;

    public Nonce getNonce() {
        return nonce;
    }

    public void setNonce(Nonce nonce) {
        this.nonce = nonce;
    }
}
