package data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionStatus {


   public  enum Status { Approved,Rejected};

     Status status;

     CombinedCreditScore creditScore;

    @JsonProperty

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonProperty
    public CombinedCreditScore getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(CombinedCreditScore creditScore) {
        this.creditScore = creditScore;
    }


    @Override
    public String toString() {
        return "TransactionStatus{" +
                "status=" + status +
                ", creditScore=" + creditScore +
                '}';
    }
}
