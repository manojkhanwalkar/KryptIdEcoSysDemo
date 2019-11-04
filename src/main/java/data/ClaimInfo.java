package data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;


public class ClaimInfo {

    private String claimId;
    private String encryptedVerifiedClaimStr;



    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }




    public String getEncryptedVerifiedClaimStr() {
        return encryptedVerifiedClaimStr;
    }

    public void setEncryptedVerifiedClaimStr(String encryptedVerifiedClaimStr) {
        this.encryptedVerifiedClaimStr = encryptedVerifiedClaimStr;
    }








    @Override
    public String toString() {
        return "ClaimRecord{" +
                "claimId='" + claimId + '\'' +
                ", EncryptedVerifiedClaimStr='" + encryptedVerifiedClaimStr + '\'' +
                '}';
    }
}
