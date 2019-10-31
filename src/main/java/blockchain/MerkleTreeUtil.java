package blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.ArrayList;
import java.util.List;

import static util.MerkleTreeUtil.getSHA2HexValue;

public class MerkleTreeUtil {


    MerkleTree tree ;


    // transaction List
        List<BinaryTreeNode> txList = new ArrayList<>();

        @JsonProperty
        // Merkle Root
                BinaryTreeNode root;


        public MerkleTreeUtil(List<Transaction> list) {


            list.forEach(t->{

                String txnHash = getSHA2HexValue(t.toString());

                BinaryTreeNode node = new BinaryTreeNode(txnHash);
                txList.add(node);
            });

            checkPowerof2();


            root = null;
        }


        private void checkPowerof2()
        {
            int size = txList.size();

            int count = 1;
            while(count<size)
            {
                count = count*2;
            }

            int diff = count-size;

            assert(diff==0);

        }

        /**
         * execute merkle_tree and set root.
         */
        public MerkleTree merkle_tree() {

            List<BinaryTreeNode> tempTxList = new ArrayList<>();

            for (int i = 0; i < this.txList.size(); i++) {
                tempTxList.add(this.txList.get(i));
            }

            List<BinaryTreeNode> newTxList = getNewTxList(tempTxList);
            while (newTxList.size() != 1) {
                newTxList = getNewTxList(newTxList);
            }

            this.root = newTxList.get(0);

            tree = new MerkleTree(this.root);

            return tree;

        }

        /**
         * return Node Hash List.
         * @param tempTxList
         * @return
         */
        private List<BinaryTreeNode> getNewTxList(List<BinaryTreeNode> tempTxList) {

            List<BinaryTreeNode> newTxList = new ArrayList<>();
            int index = 0;
            while (index < tempTxList.size()) {
                // left
                BinaryTreeNode left = tempTxList.get(index);
                index++;

                // right
                BinaryTreeNode right =  tempTxList.get(index);

                // sha2 hex value
                String sha2HexValue = getSHA2HexValue(left.getHash()+ right.getHash());

                BinaryTreeNode node = new BinaryTreeNode(sha2HexValue);
                node.left=left;
                node.right=right;
                newTxList.add(node);
                index++;

            }

            return newTxList;
        }


/*

    static  ObjectMapper mapper ;

        static {
            mapper = new ObjectMapper();
            mapper.enableDefaultTyping();


        }





    public String toJSON()
        {
            try {
                String str = mapper.writeValueAsString(root);
                return str;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return null;

        }

        public static BinaryTreeNode fromJSON(String str)
        {
            try {
                BinaryTreeNode node = mapper.readValue(str,new TypeReference<BinaryTreeNode>() {});
                return node;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    public static String toJSON(BinaryTreeNode root)
    {
        try {
            String str = mapper.writeValueAsString(root);
            return str;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static Set<FieldTuple> getFields(BinaryTreeNode root)
    {
        Set<FieldTuple> fields = new HashSet<>();

        traverseTreeAndCollectFields(root,fields);
        return fields;

    }

    public static boolean validateTree(BinaryTreeNode root)
    {
        // if left and right are not null - then compute the hash of the children and compare to value stored .
        // if not same , then return false , if same then continue checking on both left and right side of the subtree .

        return traverseAndValidateHashInTree(root);


    }

    private static boolean traverseAndValidateHashInTree(BinaryTreeNode root)
    {
        if (root.left==null && root.right==null)
        {
            return true;   // end of tree
        }
        String hash1 = root.tuple.value;
        String sha2HexValue = getSHA2HexValue(root.left.tuple.value + root.right.tuple.value);

        if (hash1.equals(sha2HexValue))
        {
            return true && traverseAndValidateHashInTree(root.left) && traverseAndValidateHashInTree(root.right);
        }
        else
        {
            return false;
        }



    }


    private static void traverseTreeAndCollectFields(BinaryTreeNode root , Set<FieldTuple> fields)
    {
        BinaryTreeNode node = root;
        if (node.left!=null)
        {
            traverseTreeAndCollectFields(node.left,fields);
        }
        if (node.right!=null)
        {
            traverseTreeAndCollectFields(node.right,fields);
        }

        if (node.tuple instanceof FieldTuple)
        {
            fields.add((FieldTuple)node.tuple);
        }

    }




    public static void main(String[] args) {

        Tuple t1 = FieldTuple.create("Name" , "Hello");
        Tuple t2 = FieldTuple.create("Address" , "Merkle");
        Tuple t3 = FieldTuple.create("DOB" , "10/10/86");

        List<Tuple> tuples = Arrays.asList(t1,t2,t3);

            MerkleTreeUtil mtu = new MerkleTreeUtil(tuples);

            mtu.merkle_tree();
            System.out.println(mtu.toJSON());

            String str = mtu.toJSON();

            BinaryTreeNode newroot = fromJSON(str);

            Set<FieldTuple> fields = getFields(newroot);

            System.out.println(fields);

            System.out.println(validateTree(newroot));

           //System.out.println(toJSON(newroot));


    }
*/


}
