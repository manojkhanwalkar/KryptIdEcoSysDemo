package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreditScore {


   int score;

    @JsonProperty

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return "CreditScore{" +
                "score=" + score +
                '}';
    }
}
