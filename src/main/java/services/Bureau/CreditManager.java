package services.Bureau;

import data.CreditScore;

import java.util.Random;

public class CreditManager {

   /* static class CreditManagerHolder {

        static CreditManager creditManager = new CreditManager();

    }

    public static CreditManager getInstance()
    {
        return CreditManagerHolder.creditManager;
    }

    private CreditManager()
    {

    }*/

   CreditManagerFactory.Bureaus bureau;

   public CreditManager(CreditManagerFactory.Bureaus bureau)
   {
       this.bureau = bureau;
   }

    Random random = new Random();

    public CreditScore getCreditScore()
    {
        // simulate actual work by sleeping
        try {
            Thread.sleep(random.nextInt(5)*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        CreditScore creditScore = new CreditScore();
        creditScore.setScore(random.nextInt(300) + 500);

        return creditScore;
    }



}
