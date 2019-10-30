package data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Claim")
public class ClaimRecord {

    private String claimId;
    private String doubleEncryptedVerifiedClaimStr;
    private String encryptedKey;


    @DynamoDBHashKey(attributeName = "ClaimId")

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }



    @DynamoDBAttribute(attributeName="DoubleEncryptedVerifiedClaim")

    public String getDoubleEncryptedVerifiedClaimStr() {
        return doubleEncryptedVerifiedClaimStr;
    }

    public void setDoubleEncryptedVerifiedClaimStr(String encryptedVerifiedClaimStr) {
        this.doubleEncryptedVerifiedClaimStr = encryptedVerifiedClaimStr;
    }


    @DynamoDBAttribute(attributeName="EncryptedKey")
    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }






    @Override
    public String toString() {
        return "ClaimRecord{" +
                "claimId='" + claimId + '\'' +
                ", DoubleEncryptedVerifiedClaimStr='" + doubleEncryptedVerifiedClaimStr + '\'' +
                '}';
    }
}
