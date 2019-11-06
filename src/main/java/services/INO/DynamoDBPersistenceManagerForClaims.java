package services.INO;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import data.*;
import util.CertUtil;
import util.CryptUtil;
import util.JSONUtil;
import util.SymKeyStringTuple;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
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





    public void deleteClaim(String claimId)
    {
        ClaimRecord claim = mapper.load(ClaimRecord.class, claimId);
        if (claim!=null)
        {
            mapper.delete(claim);
            System.out.println("Deleted claim " + claim);
        }

    }


    public void deleteClaims()
    {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<ClaimRecord> claimRecords = mapper.scan(ClaimRecord.class, scanExpression);

        claimRecords.stream().map(record->record.getClaimId()).forEach(id->{

            deleteClaim(id);
        });

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


    public Claims getAllClaims() {


        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<ClaimRecord> claimRecords = mapper.scan(ClaimRecord.class, scanExpression);

        List<ClaimInfo> claimInfos = new ArrayList<>();
        claimRecords.stream().forEach(claim->{
            ClaimInfo claimInfo = new ClaimInfo();
            String doubleEncryptedclaimStr =  claim.getDoubleEncryptedVerifiedClaimStr();
            String encryptedKey = claim.getEncryptedKey();

            String claimStr = CryptUtil.decrypt(doubleEncryptedclaimStr,encryptedKey,privateKey);

            claimInfo.setClaimId(claim.getClaimId());
            claimInfo.setEncryptedVerifiedClaimStr(claimStr);

            claimInfos.add(claimInfo);

//            EncryptedVerifiedClaim encryptedVerifiedClaim = (EncryptedVerifiedClaim)JSONUtil.fromJSON(claimStr,EncryptedVerifiedClaim.class);


        });

        Claims claims = new Claims();
        claims.setClaims((ArrayList)claimInfos);

        return claims;


    }


    public List<ClaimRecord> getAllClaimIds() {


        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<ClaimRecord> claimRecords = mapper.scan(ClaimRecord.class, scanExpression);


        return claimRecords;


    }

    public Nonces getNonces() {

        // create N nonces , save them to db and return them .

        ArrayList<Nonce> nonceList = new ArrayList<>();

        for (int i=0;i<1;i++)
        {
            NonceRecord nonceRecord = new NonceRecord();
            nonceRecord.setNonce(UUID.randomUUID().toString());
            mapper.save(nonceRecord);
            nonceList.add(new Nonce(nonceRecord.getNonce()));
        }

        Nonces nonces = new Nonces();
        nonces.setNonces(nonceList);

        return nonces;
    }

    public void deleteNonce(Nonce nonce)
    {
        // delete the nonce sent from db
        NonceRecord nonceRecord = mapper.load(NonceRecord.class,nonce.getNonce());

        mapper.delete(nonceRecord);

    }

    public boolean verifyNonce(Nonce nonce) {
        NonceRecord nonceRecord = mapper.load(NonceRecord.class,nonce.getNonce());
        if (nonceRecord==null)
        {
            System.out.println("Nonce not found " + nonce.getNonce());
            return false;
        }
        else
        {
            mapper.delete(nonceRecord);
            return true;
        }


    }
}

