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

        boolean result = isValid(identity.getSsn());

        attribNode1.setStatus((result? AttributeNode.Status.Valid: AttributeNode.Status.Fraud));



        MerkleTreeUtil util = new MerkleTreeUtil();


        MerkleTree tree = util.build(List.of(attribNode1));

        return createClaim(tree);



    }


    private  boolean isValid(String ssn)
    {
        String regex = "^(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}$";

        Pattern pattern = Pattern.compile(regex);


            Matcher matcher = pattern.matcher(ssn);

            return matcher.matches();



    }




}
