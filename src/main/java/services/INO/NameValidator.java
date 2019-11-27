package services.INO;

import data.Identity;
import data.VerifiedClaim;
import data.VerifyRequest;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.CryptUtil;
import util.MerkleTreeUtil;

import java.util.ArrayList;
import java.util.List;
import static data.Identity.PIIAttributes;


public class NameValidator extends INOMock {

    public NameValidator(String certFile1, String privateFile1)
    {
        super(certFile1,privateFile1);
    }


    public VerifiedClaim verifyIdentityAndGenerateClaim(VerifyRequest request)
    {

        simulateWorkViaRandomDelay();
        Identity identity = request.getIdentity();

        List<AttributeNode> attributeNodes = new ArrayList<>();

        if (identity.getZip()!=null)
        {
            AttributeNode attribNode1 = new AttributeNode();
            attribNode1.setName(PIIAttributes.Zip.toString());
            attribNode1.setValue(identity.getZip());
            attribNode1.setId(secureRandom.nextInt());
            attributeNodes.add(attribNode1);

        }

        if (identity.getAddress()!=null)
        {
            AttributeNode attribNode2 = new AttributeNode();
            attribNode2.setName(PIIAttributes.Address.toString());
            attribNode2.setValue(identity.getAddress());
            attribNode2.setId(secureRandom.nextInt());
            attributeNodes.add(attribNode2);


        }

        if (identity.getBirthDate()!=null)
        {
            AttributeNode attribNode3 = new AttributeNode();
            attribNode3.setName(PIIAttributes.BirthDate.toString());
            attribNode3.setValue(identity.getBirthDate().toString());
            attribNode3.setId(secureRandom.nextInt());
            attributeNodes.add(attribNode3);
        }

        if (identity.getFirstName()!=null)
        {

            AttributeNode attribNode4 = new AttributeNode();
            attribNode4.setName(PIIAttributes.FirstName.toString());
            attribNode4.setValue(identity.getFirstName());
            attribNode4.setId(secureRandom.nextInt());
            attributeNodes.add(attribNode4);
        }



       if (identity.getLastName()!=null)
       {
           AttributeNode attribNode5 = new AttributeNode();
           attribNode5.setName(PIIAttributes.LastName.toString());
           attribNode5.setValue(identity.getLastName());
           attribNode5.setId(secureRandom.nextInt());
            attributeNodes.add(attribNode5);
       }






        MerkleTreeUtil util = new MerkleTreeUtil();


        MerkleTree tree = util.build(attributeNodes);

        return createClaim(tree);



    }




}
