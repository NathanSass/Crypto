
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public class Crypto {
    private Cipher cipher;
    private final String CIPHER_MODE = "AES/CFB8/NoPadding";

    private SecretKey keySpec;
    private IvParameterSpec ivSpec;
    private Charset CHARSET = Charset.forName("UTF8");

    public Crypto(){
        String secretKey = "1234567890qwertz";
        keySpec = new SecretKeySpec(secretKey.getBytes(CHARSET), "AES");

        String iv = "1234567890qwertz";
        ivSpec = new IvParameterSpec(iv.getBytes(CHARSET));
        try {
            cipher = Cipher.getInstance(CIPHER_MODE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * @param input A "AES/CFB8/NoPadding" encrypted String
     * @return The decrypted String
     */
    public String decrypt(String input) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return  new String(cipher.doFinal(DatatypeConverter.parseBase64Binary(input)));
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * @param input Any String to be encrypted
     * @return An encrypted String
     */
    public String encrypt(String input) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return DatatypeConverter.printBase64Binary(cipher.doFinal(input.getBytes(CHARSET))).trim();
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Encrypts the keys and values of a JSONObject with Crypto.encrypt(String input)
     * @param o The JSONObject to be encrypted
     * @return A JSONObject with encrypted keys and values
     */
    public JSONObject jsonObjectEncrypt(JSONObject o) throws JSONException {

        Iterator<?> keys = o.keys();
        JSONObject returnObject = new JSONObject();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            returnObject.put(this.encrypt(key), this.encrypt(o.getString(key)));
        }

        return returnObject;
    }

    /**
     * Decrypts the keys and values of a JSONObject with Crypto.decrypt(String input)
     * @param o The JSONObject to be decrypted
     * @return A JSONObject with decrypted keys and values
     */
    public JSONObject jsonObjectDecrypt(JSONObject o) throws JSONException {

        Iterator<?> keys = o.keys();
        JSONObject returnObject = new JSONObject();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            if(key != null && !key.equals("")) {
                returnObject.put(this.decrypt(key), this.decrypt(o.getString(key)));
            }
        }

        return returnObject;
    }

    /**
     * Encrypts keys and values of every JSONObject in a JSONArray
     * @param a The JSONArray to be encrypted
     * @return A JSONArray with encrypted keys and values
     */
    public JSONArray jsonArrayEncrypt(JSONArray a) throws JSONException {
        JSONArray returnArray = new JSONArray();

        for(int i = 0; i < a.length(); i++) {
            returnArray.put(this.jsonObjectEncrypt((JSONObject)a.get(i)));
        }

        return returnArray;
    }

    /**
     * Decrypts keys and values of every JSONObject in a JSONArray
     * @param a The JSONArray to be decrypted
     * @return A JSONArray with decrypted keys and values
     */
    public JSONArray jsonArrayDecrypt(JSONArray a) throws JSONException {
        JSONArray returnArray = new JSONArray();

        for(int i = 0; i < a.length(); i++) {
            returnArray.put(this.jsonObjectDecrypt((JSONObject)a.get(i)));
        }

        return returnArray;
    }

    public static void main(String Args[]) {

        try {

            Crypto c = new Crypto();

            String original = "MiiiMüäöMeeʞ";
            System.out.println("Original: " + original);
            String encrypted = c.encrypt(original);
            System.out.println("Encoded: " + encrypted);
            System.out.println("Decoded: " + c.decrypt(encrypted));

            JSONArray arr = new JSONArray("[{\"id\"=\" 1 ʞ3 \"},{\"id\"=\"4\"}]");
            System.out.println(c.jsonArrayDecrypt(c.jsonArrayEncrypt(arr)).getJSONObject(0).getString("id"));
            System.out.println(c.jsonArrayDecrypt(c.jsonArrayEncrypt(arr)).getJSONObject(1).getString("id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
