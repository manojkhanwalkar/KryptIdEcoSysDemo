package services.client;

import data.Identity;
import data.VerifyRequest;
import util.JSONUtil;
import util.SampleData;

import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.*;

public class ClientTester {

    public static void main(String[] args) throws Exception {
        ClientTester clientTester = new ClientTester();

        clientTester.test();
    }

    List<String> claims = new ArrayList<>();

    public synchronized void addClaim(String claimId)
    {
        claims.add(claimId);

    }

    Random random = new Random();
    public synchronized String getRandomClaim()
    {
        int index = random.nextInt(claims.size());
        return claims.get(index);

    }

    CountDownLatch verifyCount = new CountDownLatch(100);
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ExecutorService executorService1 = Executors.newCachedThreadPool();


    public void test() throws Exception
    {
        VerifyTester verifyTester = new VerifyTester(this);

        executorService.scheduleAtFixedRate(verifyTester,1,1, TimeUnit.SECONDS);

        Thread.sleep(10000);
        for (int i=0;i<100;i++)
        {
            TransactTester transactTester = new TransactTester(this);
            executorService1.submit(transactTester);

            if (i%5==0)
                Thread.sleep(10000);

            //TODO - add phasers here

        }



        try {
            verifyCount.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}

class TransactTester implements Runnable
{
    ClientTester clientTester;

    public TransactTester(ClientTester clientTester)
    {
        this.clientTester = clientTester;
    }


    public void run()
    {
        Connection connection = new Connection("https://localhost:8780/");
        try {
            String transactionStatus = connection.sendSimple(clientTester.getRandomClaim(),"transact");
            System.out.println("Received " + transactionStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}


class VerifyTester implements Runnable
{



    private static VerifyRequest makeSampleRequest()
    {
        VerifyRequest request = new VerifyRequest();

        Identity identity = SampleData.sampleIdentityGenerator();

        request.setIdentity(identity);



        return request;
    }

    ClientTester clientTester;

    public VerifyTester(ClientTester clientTester)
    {
        this.clientTester = clientTester;
    }


    public void run()
    {
        Connection connection = new Connection("https://localhost:8780/");
            VerifyRequest verifyRequest = makeSampleRequest();
        try {
            String claimId = connection.sendSimple(JSONUtil.toJSON(verifyRequest),"verify");
            System.out.println("Received " + claimId);
            clientTester.addClaim(claimId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        clientTester.verifyCount.countDown();

    }
}