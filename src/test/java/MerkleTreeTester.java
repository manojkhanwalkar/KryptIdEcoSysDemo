
import data.VerifiedClaim;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.JSONUtil;
import util.MerkleTreeUtil;
import util.RandomIdGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MerkleTreeTester {

  public static void main(String[] args)  throws Exception {

      MerkleTreeTester tester = new MerkleTreeTester();

      tester.build();
  }


  public void build() throws Exception
  {
      AttributeNode attribNode1 = new AttributeNode();
      attribNode1.setName("FNAME");
      attribNode1.setValue("Hello");
      attribNode1.setId(RandomIdGenerator.getId());

      AttributeNode attribNode2 = new AttributeNode();
      attribNode2.setName("LNAME");
      attribNode2.setValue("World");
      attribNode2.setId(RandomIdGenerator.getId());


      AttributeNode attribNode3 = new AttributeNode();
      attribNode3.setName("SSN");
      attribNode3.setValue("100-20-300");
      attribNode3.setId(RandomIdGenerator.getId());

      MerkleTreeUtil util = new MerkleTreeUtil();
      MerkleTree tree = util.build(List.of(attribNode1,attribNode2,attribNode3));


      VerifiedClaim verifiedClaim = new VerifiedClaim();
      verifiedClaim.setTree(tree);

      String wrapstr = JSONUtil.toJSON(verifiedClaim);

      verifiedClaim = (VerifiedClaim) JSONUtil.fromJSON(wrapstr,VerifiedClaim.class);
      tree = verifiedClaim.getTree();

      String str = MerkleTreeUtil.toJSON(tree);
      System.out.println(str);

     MerkleTree reconTree = MerkleTreeUtil.copy(tree);
      String str1 = MerkleTreeUtil.toJSON(reconTree);
      System.out.println(str1);

      Set<AttributeNode> nodes = new HashSet<>();
      nodes.addAll(List.of(attribNode1,attribNode2,attribNode3));
      MerkleTree partialTree = util.buildPartial(tree,nodes);

     String str2 = MerkleTreeUtil.toJSON(partialTree);

      System.out.println(str2);

      //System.out.println(str1.equals(str2));


    System.out.println(MerkleTreeUtil.validateTree(tree));
     System.out.println( MerkleTreeUtil.validateTree(reconTree));
      System.out.println(MerkleTreeUtil.validateTree(partialTree));

     System.out.println(MerkleTreeUtil.getAttributes(tree));




  }
}
