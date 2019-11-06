package data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Nonce")
public class NonceRecord {

    private String nonce;



    @DynamoDBHashKey(attributeName = "nonce")

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "ClaimRecord{" +
                "nonce='" + nonce + '\'' +
                '}';
    }
}
