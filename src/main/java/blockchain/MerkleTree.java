package blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MerkleTree {

    @JsonProperty
    BinaryTreeNode root;

    public MerkleTree(BinaryTreeNode root)
    {
        this.root = root;
    }

    public MerkleTree()
    {

    }




}
