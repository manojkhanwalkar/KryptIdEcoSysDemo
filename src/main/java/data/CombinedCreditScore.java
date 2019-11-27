package data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class CombinedCreditScore {


   int score;

    @JsonProperty

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    ArrayList<CreditScore> creditScores = new ArrayList<>();

    public synchronized void addScore(CreditScore creditScore)
    {
        creditScores.add(creditScore);

         score = creditScores.stream().mapToInt(cs1->creditScore.getScore()).reduce(0,(x,y)->x+y);
    }



    public ArrayList<CreditScore> getCreditScores() {
        return creditScores;
    }

    public void setCreditScores(ArrayList<CreditScore> creditScores) {
        this.creditScores = creditScores;
    }

    @Override
    public String toString() {
        return "CombinedCreditScore{" +
                "score=" + score +
                ", creditScores=" + creditScores +
                '}';
    }
}
