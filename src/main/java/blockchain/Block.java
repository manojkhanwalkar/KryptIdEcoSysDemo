package blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block {


    @JsonProperty
    String rootHash;

    @JsonProperty
    String prevHash;

    @JsonProperty
    MerkleTree tree;


    @JsonProperty
    Map<String,Transaction> transactionMap = new HashMap<>();


    public Block(List<Transaction> transactions)
    {

        MerkleTreeUtil mtu = new MerkleTreeUtil(transactions);
        tree = mtu.merkle_tree();
        transactions.stream().forEach(t-> {


            transactionMap.put(t.getTransactionId(),t);

            });
    }


    public Block()
    {

    }

    public static Block getGenesisBlock()
    {
        Block genesis = new Block();
        genesis.prevHash="-1";
        genesis.rootHash="0";
        genesis.tree = new MerkleTree();

        return genesis;
    }


}
