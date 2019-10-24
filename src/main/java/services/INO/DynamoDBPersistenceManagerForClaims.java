package services.INO;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import data.ClaimRecord;
import data.EncryptedVerifiedClaim;
import data.Identity;
import data.VerifyRequest;
import util.JSONUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DynamoDBPersistenceManagerForClaims {

    AmazonDynamoDB ddb;


    static AmazonDynamoDB getHandle()
    {
        AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
                .build();

        return ddb;

    }

    DynamoDBMapper mapper;

    public DynamoDBPersistenceManagerForClaims()
    {
        ddb = getHandle();
        mapper = new DynamoDBMapper(ddb);

    }


    public void createClaim(String claimId, EncryptedVerifiedClaim encryptedVerifiedClaim)
    {


        ClaimRecord claim = new ClaimRecord();
        claim.setClaimId(claimId);
        claim.setEncryptedVerifiedClaimStr(JSONUtil.toJSON(encryptedVerifiedClaim));

        mapper.save(claim);

    }









    public EncryptedVerifiedClaim getClaim(String claimId)
    {

            ClaimRecord claim = mapper.load(ClaimRecord.class, claimId);

            String claimStr =  claim.getEncryptedVerifiedClaimStr();

            EncryptedVerifiedClaim encryptedVerifiedClaim = (EncryptedVerifiedClaim)JSONUtil.fromJSON(claimStr,EncryptedVerifiedClaim.class);

            return encryptedVerifiedClaim;
    }



}

