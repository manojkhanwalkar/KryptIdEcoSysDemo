package services.client.wallet;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.*;
import data.SimpleWalletRecord;
import util.CertUtil;
import util.CryptUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/*
Single private key stored in db
 */
public class SimpleWallet {




    static AmazonDynamoDB getHandle()
    {
        AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
                .build();

        return ddb;

    }


    DynamoDBMapper mapper;

    AmazonDynamoDB ddb;

    PublicKey rsaPublicKey;
    PrivateKey rsaPrivateKey;

    X509Certificate rsaCertificate;

    SimpleWalletRecord simpleWalletRecord;

    public SimpleWallet()
    {
        ddb = getHandle();
        mapper = new DynamoDBMapper(ddb);

        simpleWalletRecord =  mapper.load(SimpleWalletRecord.class, "1");

        rsaPublicKey = CryptUtil.convertStringtoPublicKey(simpleWalletRecord.getPublicKey(),"RSA");
        rsaPrivateKey = CryptUtil.convertStringtoPrivateKey(simpleWalletRecord.getPrivateKey(),"RSA");

        rsaCertificate = CertUtil.getCertFromString(simpleWalletRecord.getCertificate());


    }


    static    String table_name = "SimpleWallet";


    public PublicKey getRsaPublicKey()
    {
        return rsaPublicKey;
    }


    public PrivateKey getRsaPrivateKey()
    {
        return rsaPrivateKey;
    }

    public String getClaimId()
    {
        return simpleWalletRecord.getClaimId();
    }

    public X509Certificate getRsaCertificate() {
        return rsaCertificate;
    }

    public void updateClaimId(String claimId)
    {


        simpleWalletRecord.setClaimId(claimId);
        mapper.save(simpleWalletRecord);


    }










    // send verify request and get claim id from the INO and update the record .

    // retrieve record to get the claim and then send the claim for transacting .

    // if claim has expired , then a new claimId will be generated - update the record with the new claim Id .






}
