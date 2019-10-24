package data;

public class TransactionNumber {

    String transactionId ;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }


    @Override
    public String toString() {
        return "TransactionNumber{" +
                "transactionId='" + transactionId + '\'' +
                '}';
    }
}
