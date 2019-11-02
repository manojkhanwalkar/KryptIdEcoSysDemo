package services.INO;

import data.Identity;
import data.VerifiedClaim;
import data.VerifyRequest;
import shareablemerkletree.AttributeNode;
import shareablemerkletree.MerkleTree;
import util.CryptUtil;
import util.MerkleTreeUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static data.Identity.PIIAttributes;


public class EmailValidator extends INOMock {

    public EmailValidator(String certFile1, String privateFile1)
    {
        super(certFile1,privateFile1);
    }


    public VerifiedClaim verifyIdentityAndGenerateClaim(VerifyRequest request)
    {

        Identity identity = request.getIdentity();

        AttributeNode attribNode1 = new AttributeNode();
        if (identity.getEmail()!=null) {
            attribNode1.setName(PIIAttributes.Email.toString());
            attribNode1.setValue(identity.getEmail());
            attribNode1.setId(secureRandom.nextInt());
        }

        boolean result = isValid(identity.getEmail());
        attribNode1.setStatus((result? AttributeNode.Status.Valid: AttributeNode.Status.Fraud));



        MerkleTreeUtil util = new MerkleTreeUtil();


        MerkleTree tree = util.build(List.of(attribNode1));

        return createClaim(tree);
    }



    private  boolean isValid(String email)
    {
        String regex =  "^[A-Za-z0-9+_.-]+@(A-Za-z0-9+_.-)+[a-zA-Z]{2,6}$";



        Pattern pattern = Pattern.compile(regex);


        Matcher matcher = pattern.matcher(email);

        return matcher.matches();



    }




}
