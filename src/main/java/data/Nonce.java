package data;


public class Nonce {

    public Nonce()
    {

    }

    public Nonce(String nonce) {
        this.nonce = nonce;
    }

    private String nonce;

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "Nonce{" +
                "nonce='" + nonce + '\'' +
                '}';
    }
}
