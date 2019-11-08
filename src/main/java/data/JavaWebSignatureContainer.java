package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JavaWebSignatureContainer {

  //  String publicKey;
    String signature;


    String certStr;

    public String getCertStr() {
        return certStr;
    }

    public void setCertStr(String certStr) {
        this.certStr = certStr;
    }





    @JsonProperty
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }


}
