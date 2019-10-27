package util;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import data.SimpleWalletRecord;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/*
Single private key stored in db
 */
public class SimpleWalletCreator {

    String rsaCertFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/userrsacertificate.pem";
    String rsaPrivateFile = "/home/manoj/IdeaProjects/KryptIdEcoSysDemo/src/main/resources/userrsakey.der";



    public static void main(String[] args) {
       // createTable();

        SimpleWalletCreator wallet = new SimpleWalletCreator();


      //  wallet.insertKeys();

        wallet.printKeys();


    }

    public void function(final AmazonDynamoDB client, final String tableName, final String ttlField){

        //table created now enabling TTL
        final UpdateTimeToLiveRequest req = new UpdateTimeToLiveRequest();
        req.setTableName(tableName);

        final TimeToLiveSpecification ttlSpec = new TimeToLiveSpecification();
        ttlSpec.setAttributeName(ttlField);
        ttlSpec.setEnabled(true);

        req.withTimeToLiveSpecification(ttlSpec);

        client.updateTimeToLive(req);
    }


    public void printKeys()
    {


            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            List<SimpleWalletRecord> scanResult = mapper.scan(SimpleWalletRecord.class, scanExpression);

            scanResult.stream().forEach(r-> System.out.println(r));



    }

    // create table SimpleWallet

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
    X509Certificate certificate;

    public SimpleWalletCreator()
    {
        ddb = getHandle();
        mapper = new DynamoDBMapper(ddb);

    }


    static    String table_name = "SimpleWallet";


    public void insertKeys()
    {

        rsaPublicKey = CryptUtil.getPublicKeyFromCertFile(rsaCertFile,"RSA");
        rsaPrivateKey = CryptUtil.getPrivateKeyDerFromFile(rsaPrivateFile,"RSA");
        certificate = CertUtil.getCertificate(rsaCertFile);

        String privateKey = CryptUtil.convertPrivateKeyToString(rsaPrivateKey);
        String publicKey = CryptUtil.convertPublicKeyToString(rsaPublicKey);

        String certStr = CertUtil.getCertAsString(certificate);



        SimpleWalletRecord simpleWalletRecord = new SimpleWalletRecord();

        simpleWalletRecord.setId("2");
        simpleWalletRecord.setPrivateKey(privateKey);
        simpleWalletRecord.setPublicKey(publicKey);
        simpleWalletRecord.setCertificate(certStr);

        mapper.save(simpleWalletRecord);

    }



    public static void createTable()
    {



        List<AttributeDefinition> definitions = new ArrayList<>();
        definitions.add(new AttributeDefinition(
                "id", ScalarAttributeType.S));
       /* definitions.add(new AttributeDefinition(
                "Identity", ScalarAttributeType.S)); */

        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(definitions)
                .withKeySchema(new KeySchemaElement("id", KeyType.HASH))
                .withProvisionedThroughput(new ProvisionedThroughput(
                        new Long(10), new Long(10)))
                .withTableName(table_name);

        final AmazonDynamoDB ddb = getHandle();




        try {
            CreateTableResult result = ddb.createTable(request);
            System.out.println(result.getTableDescription());
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
    }









}
