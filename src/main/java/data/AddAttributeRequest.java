package data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class AddAttributeRequest {

    @JsonProperty
    String claimId;

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    @JsonProperty
    HashMap<Identity.PIIAttributes,String> attributes  = new HashMap<>();

    public HashMap<Identity.PIIAttributes, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<Identity.PIIAttributes, String> attributes) {
        this.attributes = attributes;
    }

    public String get(Identity.PIIAttributes attrName)
    {
        return attributes.get(attrName);
    }

    public void put(Identity.PIIAttributes attrName , String value)
    {
        attributes.put(attrName,value);
    }
}
