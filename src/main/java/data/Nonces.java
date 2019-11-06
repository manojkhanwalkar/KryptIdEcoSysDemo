package data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Nonces {


    @JsonProperty
    ArrayList<Nonce> nonces = new ArrayList();

    @Override
    public String toString() {
        return "Nonces{" +
                "nonces=" + nonces +
                '}';
    }

    public ArrayList<Nonce> getNonces() {
        return nonces;
    }

    public void setNonces(ArrayList<Nonce> nonces) {
        this.nonces = nonces;
    }
}
