package data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Claim")
public class ClaimRecord {

    private String claimId;
    private String encryptedVerifiedClaimStr;


    @DynamoDBHashKey(attributeName = "ClaimId")

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }



    @DynamoDBAttribute(attributeName="EncryptedVerifiedClaim")

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
                ", encryptedVerifiedClaimStr='" + encryptedVerifiedClaimStr + '\'' +
                '}';
    }
}
