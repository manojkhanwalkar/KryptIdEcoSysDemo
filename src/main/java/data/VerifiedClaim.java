package data;

//import shareablemerkle.MerkleTree;

import shareablemerkletree.MerkleTree;

public class VerifiedClaim {


    public static String pattern = "dd-MM-yyyy HH:mm:ss";

    MerkleTree tree;

    public MerkleTree getTree() {
        return tree;
    }

    public void setTree(MerkleTree tree) {
        this.tree = tree;
    }

    String validUntil;




    String signature;

    String publicKey;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }


    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public String toString() {
        return "VerifiedClaim{" +
                "tree=" + tree +
                ", signature='" + signature + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }


    /*

    public static void main(String[] args) {





    }


     */

}
