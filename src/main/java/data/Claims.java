package data;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Claims {

    ArrayList<ClaimInfo> claims = new ArrayList<>();

    public ArrayList<ClaimInfo> getClaims() {
        return claims;
    }

    public void setClaims(ArrayList<ClaimInfo> claims) {
        this.claims = claims;
    }
}
