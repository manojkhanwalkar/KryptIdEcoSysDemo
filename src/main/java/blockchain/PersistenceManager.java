package blockchain;


public interface PersistenceManager {

    public void persist(Block block);
    public BlockChain restore();
}
