package data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ClaimIds {


    @JsonProperty
    ArrayList<String> claimsIds = new ArrayList();

    public ArrayList<String> getClaimsIds() {
        return claimsIds;
    }

    public void setClaimsIds(ArrayList<String> claimsIds) {
        this.claimsIds = claimsIds;
    }

    @Override
    public String toString() {
        return "ClaimIds{" +
                "claimsIds=" + claimsIds +
                '}';
    }
}
