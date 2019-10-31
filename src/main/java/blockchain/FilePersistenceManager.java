package blockchain;


import org.apache.commons.io.comparator.LastModifiedFileComparator;
import util.JSONUtil;

import java.io.*;
import java.util.Arrays;

public class FilePersistenceManager implements PersistenceManager {

    String persistenceDir ;

    public FilePersistenceManager(String persistenceDir)
    {
        this.persistenceDir = persistenceDir;
    }


    // new file everytime we persist = bc + timestamp

    // restore one file at a time , sorted by time .

        @Override
    public void persist(Block block) {

            try {
                File file = new File(persistenceDir + System.currentTimeMillis() + ".data");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));

                // store one block per line .

                {

                    String str = JSONUtil.toJSON(block);
                    try {
                        writer.write(str);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
               writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        int numBlocks=0;

    @Override
    public BlockChain restore()
    {

        BlockChain blockChain=null;
        try {
            File dir = new File(persistenceDir);
            File[] files = dir.listFiles();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);


            if (files.length==0) {

                return null;

            }

            blockChain = new BlockChain();
            for (File file : files ) {

                 BufferedReader reader = new BufferedReader(new FileReader(file));
                 String s = reader.readLine();
                 while (s != null) {
                     Block block = (Block) JSONUtil.fromJSON(s, Block.class);
                     blockChain.restore(block);
                     s = reader.readLine();
                     numBlocks++;
                 }

             }

            System.out.println("Number of blocks read are " + numBlocks);



            return blockChain;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return blockChain;


    }





/*    @Override
    public void persist(BlockChain blockchain) {

        File file = new File("/home/manoj/data/blockchain/bc.data");

        JSONUtil.toFileAsJSON(blockchain,file);




    }

    @Override
    public BlockChain restore()
    {
        File file = new File("/home/manoj/data/blockchain/bc.data");
        BlockChain blockChain;

        if (file.exists()) {

             blockChain = (BlockChain) JSONUtil.fromFileAsJSONObject(file, BlockChain.class);
        }
        else
        {
             blockChain = new BlockChain();
             blockChain.bootstrap();
        }

        return blockChain;
    }*/




}
