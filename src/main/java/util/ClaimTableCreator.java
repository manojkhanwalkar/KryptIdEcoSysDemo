package util;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import data.ClaimRecord;
import data.Identity;
import data.VerifyRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create a DynamoDB table.
 *
 * Takes the name of the table to create. The table will contain a single
 * primary key, "Name".
 *
 * This code expects that you have AWS credentials set up per:
 * http://docs.aws.amazon.com/java-sdk/latest/developer-guide/setup-credentials.html
 */
public class ClaimTableCreator
{

    static AmazonDynamoDB getHandle()
    {
        AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
                .build();

        return ddb;

    }


    static    String table_name = "Claim";





    public static void main(String[] args) throws Exception {


       createTable();

    //  printAllClaims();

       //seedIdentity();

    // deleteTable();


    //   printItems();

//        printItemsMatchingFilter();

    //    testMarkFraud();

  //      testIdentityVerification();




    }



    public static void printAllClaims()
    {

        final AmazonDynamoDB ddb = getHandle();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<ClaimRecord> scanResult = mapper.scan(ClaimRecord.class, scanExpression);

       scanResult.stream().forEach(r-> System.out.println(r));


    }


/*    public static void testMarkFraud()
    {
        FraudRequest request = new FraudRequest();

        request.setPin("64");

        PIDDynamoDBPersistenceManager manager = new PIDDynamoDBPersistenceManager();
        FraudResponse response = manager.markFraud(request);

        System.out.println(response);

    } */


  /*  public static void testIdentityVerification()
    {
        VerifyRequest request = new VerifyRequest();
        String str = "{\"firstName\":\"whuwm\",\"lastName\":\"wtscv\",\"birthDate\":[1978,3,19],\"address\":\"9 iqwjve Ave\",\"zip\":\"92570\"}";

        Identity identity = (Identity) JSONUtil.fromJSON(str, Identity.class);

        request.setIdentity(identity);

        PIDDynamoDBPersistenceManager manager = new PIDDynamoDBPersistenceManager();
        VerifyResponse response = manager.verifyIdentity(request);

        System.out.println(response);
    } */


    /*
    public static void printItems()
    {


        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        final AmazonDynamoDB ddb = getHandle();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        List<IdTuple> latestReplies = mapper.scan(IdTuple.class, scanExpression);


        latestReplies.stream().forEach(s-> System.out.println(s));


    }*/


 /*   public static void printItemsMatchingFilter()
    {

        final AmazonDynamoDB ddb = getHandle();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);



        String str = "{\"firstName\":\"whuwm\",\"lastName\":\"wtscv\",\"birthDate\":[1978,3,19],\"address\":\"9 iqwjve Ave\",\"zip\":\"92570\"}";

        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(str));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("useridentity = :val1").withExpressionAttributeValues(eav);

        List<IdTuple> scanResult = mapper.scan(IdTuple.class, scanExpression);


        scanResult.stream().forEach(s->System.out.println(s));

    }*/



    public static void deleteTable() {

        final AmazonDynamoDB ddb = getHandle();


        DeleteTableResult result = ddb.deleteTable(table_name);

        System.out.println(result);
    }


 /*   public static void seedIdentity()
    {
        final AmazonDynamoDB ddb = getHandle();

        DynamoDBMapper mapper = new DynamoDBMapper(ddb);


        SampleData sample = new SampleData();

        for (int i=0;i<100;i++) {
            Identity identity = new Identity();
            identity.setFirstName(sample.getFirstName());
            identity.setLastName(sample.getFirstName());
            identity.setAddress(sample.getAddress());
            identity.setBirthDate(sample.getDate());
            identity.setZip(sample.getZipCode());

            String str = JSONUtil.toJSON(identity);

            identity = (Identity) JSONUtil.fromJSON(str, Identity.class);

            System.out.println(str);

            IdTuple tuple = new IdTuple();


            tuple.setPin(String.valueOf(i));
            tuple.setIdentity(str);
            tuple.setStatus(Status.Good.toString());

            mapper.save(tuple);

        }




    } */



    public static void createTable()
    {



        List<AttributeDefinition> definitions = new ArrayList<>();
        definitions.add(new AttributeDefinition(
                "ClaimId", ScalarAttributeType.S));
       /* definitions.add(new AttributeDefinition(
                "Identity", ScalarAttributeType.S)); */

        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(definitions)
                .withKeySchema(new KeySchemaElement("ClaimId", KeyType.HASH))
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