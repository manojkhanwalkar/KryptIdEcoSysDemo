package util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;

public class HMACUtil {

    private static final String HMAC_SHA512 = "HmacSHA512";

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String calculateHMAC(String data, String key)

    {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA512);
            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(secretKeySpec);
            return toHexString(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }


    /** Environment variable (or system property {@code kryptid.hmac.key}) holding the shared HMAC key. */
    public static final String HMAC_KEY_ENV = "KRYPTID_HMAC_KEY";

    /**
     * The shared request-HMAC key. It is deliberately not hard-coded: a key in source is known to anyone
     * with the repository. Returns null when not configured; servers must then reject HMAC-protected calls.
     */
    public static String configuredKey()
    {
        String key = System.getProperty("kryptid.hmac.key");
        if (key == null || key.isEmpty())
            key = System.getenv(HMAC_KEY_ENV);
        if (key == null || key.length() < 32)
            return null;   // missing or too short to be a real secret
        return key;
    }

    /** Constant-time HMAC check; false for a missing value or key. */
    public static boolean isValid(String hmac , String data , String key)
    {
        if (hmac == null || data == null || key == null)
            return false;
        String computed = calculateHMAC(data,key);
        if (computed == null)
            return false;
        return java.security.MessageDigest.isEqual(computed.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                hmac.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        String hmac = calculateHMAC("data", "key");
        System.out.println(hmac);
    }

}
