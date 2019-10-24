package services.INO;

import data.Identity;
import data.VerifiedClaim;
import data.VerifyRequest;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.CryptUtil;
import util.MerkleTreeUtil;

import java.util.List;

public class NameValidator extends INOMock {

    public NameValidator(String certFile1, String privateFile1)
    {
        super(certFile1,privateFile1);
    }


    public VerifiedClaim verifyIdentityAndGenerateClaim(VerifyRequest request)
    {

        Identity identity = request.getIdentity();



        AttributeNode attribNode1 = new AttributeNode();
        attribNode1.setName("Zip");
        attribNode1.setValue(identity.getZip());
        attribNode1.setId(secureRandom.nextInt());

        AttributeNode attribNode2 = new AttributeNode();
        attribNode2.setName("Address");
        attribNode2.setValue(identity.getAddress());
        attribNode2.setId(secureRandom.nextInt());

        AttributeNode attribNode3 = new AttributeNode();
        attribNode3.setName("BirthDate");
        attribNode3.setValue(identity.getBirthDate().toString());
        attribNode3.setId(secureRandom.nextInt());

        AttributeNode attribNode4 = new AttributeNode();
        attribNode4.setName("FirstName");
        attribNode4.setValue(identity.getFirstName());
        attribNode4.setId(secureRandom.nextInt());


        AttributeNode attribNode5 = new AttributeNode();
        attribNode5.setName("LastName");
        attribNode5.setValue(identity.getLastName());
        attribNode5.setId(secureRandom.nextInt());



        MerkleTreeUtil util = new MerkleTreeUtil();


        MerkleTree tree = util.build(List.of(attribNode1,attribNode2,attribNode3,attribNode4,attribNode5));

        VerifiedClaim verifiedClaim = new VerifiedClaim();
        verifiedClaim.setTree(tree);

        String toSign = tree.getRoot().getHash();

        String signature = verifiedClaimSignUtil.sign(toSign);


        verifiedClaim.setSignature(signature);

        String publicKeyStr = CryptUtil.convertPublicKeyToString(verifiedClaimSignUtil.getPublicKey());

        verifiedClaim.setPublicKey(publicKeyStr);

        return verifiedClaim;

    }




}
