package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionStatus {


   public  enum Status { Approved,Rejected};

     Status status;

    @JsonProperty

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "TransactionStatus{" +
                "status=" + status +
                '}';
    }
}
