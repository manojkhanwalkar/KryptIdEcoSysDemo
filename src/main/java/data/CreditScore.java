package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import services.Bureau.CreditManagerFactory;

public class CreditScore {


   int score;

    @JsonProperty

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    CreditManagerFactory.Bureaus bureau;

    public CreditManagerFactory.Bureaus getBureau() {
        return bureau;
    }

    public void setBureau(CreditManagerFactory.Bureaus bureau) {
        this.bureau = bureau;
    }

    @Override
    public String toString() {
        return "CreditScore{" +
                "score=" + score +
                ", bureau=" + bureau +
                '}';
    }
}
