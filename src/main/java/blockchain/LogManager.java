package blockchain;

import data.VerifyRequest;
import util.JSONUtil;

import java.util.UUID;

import static util.MerkleTreeUtil.getSHA2HexValue;

public class LogManager {



    public void submit(VerifyRequest verifyRequest)
    {
        Transaction transaction = new Transaction();

        String id = UUID.randomUUID().toString();


        transaction.setTransactionId(id);
        transaction.setContents(JSONUtil.toJSON(verifyRequest));

        String hash = getSHA2HexValue(transaction.getTransactionId() + transaction.getContents()) ;
        transaction.setHash(hash);

        TransactionManager.getInstance().submit(transaction);


    }

}
