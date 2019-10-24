package services.INO;

import data.Identity;
import data.VerifiedClaim;
import data.VerifyRequest;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.CryptUtil;
import util.MerkleTreeUtil;

import java.util.List;

public class SSNValidator extends INOMock {

    public SSNValidator(String certFile1, String privateFile1)
    {
        super(certFile1,privateFile1);
    }


    public VerifiedClaim verifyIdentityAndGenerateClaim(VerifyRequest request)
    {

        Identity identity = request.getIdentity();



        AttributeNode attribNode1 = new AttributeNode();
        attribNode1.setName("SSN");
        attribNode1.setValue(identity.getSsn());
        attribNode1.setId(secureRandom.nextInt());



        MerkleTreeUtil util = new MerkleTreeUtil();


        MerkleTree tree = util.build(List.of(attribNode1));

        return createClaim(tree);



    }




}
