package blockchain;

public  class BinaryTreeNode
{

   String hash;

    public BinaryTreeNode left = null;
    public BinaryTreeNode right = null;

    public BinaryTreeNode(String hash)
    {
        this.hash = hash;

    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BinaryTreeNode()
    {

    }


}
