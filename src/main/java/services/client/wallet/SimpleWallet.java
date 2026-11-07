package services.client.wallet;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import data.SimpleWalletRecord;
import javassist.Loader;
import util.CertUtil;
import util.CertificateCreator;
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


    static DynamoDBMapper mapper;

    static AmazonDynamoDB ddb;

    PublicKey rsaPublicKey;
    PrivateKey rsaPrivateKey;
    String claimId;

    X509Certificate rsaCertificate;


    static
    {
        ddb = getHandle();
        mapper = new DynamoDBMapper(ddb);


    }


    public static SimpleWallet createWallet()
    {
        SimpleWallet wallet = new SimpleWallet();
        CertificateCreator certificateCreator = new CertificateCreator();
        wallet.setRsaPublicKey(certificateCreator.getPublicKey());
        wallet.setRsaPrivateKey(certificateCreator.getPrivateKey());
        wallet.setRsaCertificate(certificateCreator.getCertificate());
        return wallet;

    }

    public static SimpleWallet getWallet(String claimId)
    {
        SimpleWallet wallet = new SimpleWallet();

        SimpleWalletRecord simpleWalletRecord =  mapper.load(SimpleWalletRecord.class, claimId);

        wallet.rsaPublicKey = CryptUtil.convertStringtoPublicKey(simpleWalletRecord.getPublicKey(),"RSA");
        wallet.rsaPrivateKey = CryptUtil.convertStringtoPrivateKey(simpleWalletRecord.getPrivateKey(),"RSA");
        wallet.rsaCertificate = CertUtil.getCertFromString(simpleWalletRecord.getCertificate());

        return wallet;

    }


    public void setRsaPublicKey(PublicKey rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }

    public void setRsaPrivateKey(PrivateKey rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }

    public void setRsaCertificate(X509Certificate rsaCertificate) {
        this.rsaCertificate = rsaCertificate;
    }

    public void setClaimId(String claimId)
    {
        this.claimId = claimId;
    }

    public static void updateKeys(SimpleWallet wallet)
    {

        SimpleWalletRecord simpleWalletRecord = new SimpleWalletRecord();
        simpleWalletRecord.setCertificate(CertUtil.getCertAsString(wallet.rsaCertificate));
        simpleWalletRecord.setPublicKey(CryptUtil.convertPublicKeyToString(wallet.rsaPublicKey));
        simpleWalletRecord.setPrivateKey(CryptUtil.convertPrivateKeyToString(wallet.rsaPrivateKey));
        simpleWalletRecord.setClaimId(wallet.claimId);
        mapper.save(simpleWalletRecord);

    }

    public static void deleteWalletKey(String claimId)
    {
        SimpleWalletRecord simpleWalletRecord =  mapper.load(SimpleWalletRecord.class, claimId);

        if (simpleWalletRecord!=null) {

            mapper.delete(simpleWalletRecord);
            System.out.println("Deleted wallet key for claim " + simpleWalletRecord.getClaimId());
        }

    }

    public static void deleteWallet()
    {

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<SimpleWalletRecord> walletRecords = mapper.scan(SimpleWalletRecord.class, scanExpression);

        walletRecords.stream().map(record -> record.getClaimId()).forEach(id->deleteWalletKey(id));


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
        return claimId;
    }

    public X509Certificate getRsaCertificate() {
        return rsaCertificate;
    }








}
