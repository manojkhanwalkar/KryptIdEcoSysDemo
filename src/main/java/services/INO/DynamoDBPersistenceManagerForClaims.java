package services.INO;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import data.ClaimRecord;
import data.EncryptedVerifiedClaim;
import data.Identity;
import data.VerifyRequest;
import util.CertUtil;
import util.CryptUtil;
import util.JSONUtil;
import util.SymKeyStringTuple;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DynamoDBPersistenceManagerForClaims {


    String certFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/dbrsacertificate.pem";
    String privateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/dbrsakey.der";



    AmazonDynamoDB ddb;


    PublicKey publicKey;
    PrivateKey privateKey;
    X509Certificate certificate;

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
        publicKey =  CryptUtil.getPublicKeyFromCertFile(certFile,"RSA");
        certificate = CertUtil.getCertificate(certFile);
        privateKey =  CryptUtil.getPrivateKeyDerFromFile(privateFile,"RSA");

    }


    public void createClaim(String claimId, EncryptedVerifiedClaim encryptedVerifiedClaim)
    {


        ClaimRecord claim = new ClaimRecord();
        claim.setClaimId(claimId);

        String str = JSONUtil.toJSON(encryptedVerifiedClaim);
        SymKeyStringTuple tuple = CryptUtil.encrypt(str,publicKey);
        claim.setDoubleEncryptedVerifiedClaimStr(tuple.message);
        claim.setEncryptedKey(tuple.key);

        mapper.save(claim);

    }









    public EncryptedVerifiedClaim getClaim(String claimId)
    {

            ClaimRecord claim = mapper.load(ClaimRecord.class, claimId);

            String doubleEncryptedclaimStr =  claim.getDoubleEncryptedVerifiedClaimStr();
            String encryptedKey = claim.getEncryptedKey();

            String claimStr = CryptUtil.decrypt(doubleEncryptedclaimStr,encryptedKey,privateKey);

            EncryptedVerifiedClaim encryptedVerifiedClaim = (EncryptedVerifiedClaim)JSONUtil.fromJSON(claimStr,EncryptedVerifiedClaim.class);

            return encryptedVerifiedClaim;
    }


    public List<ClaimRecord> getAllClaimIds() {


        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<ClaimRecord> claimRecords = mapper.scan(ClaimRecord.class, scanExpression);


        return claimRecords;


    }
}

