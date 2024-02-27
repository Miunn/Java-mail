package PKG;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.json.Json;
import javax.json.JsonObject;

public class Client {

    private String identity;

    Client(String identity) {
        this.identity = identity;

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException|NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public String getidentity() {
        return identity;
    }

    public JsonObject getClientPublicJSON() {
        return Json.createObjectBuilder()
                    .add("identity", this.identity)
                    .add("pk", "0000-0000-0000-0000")
                    .build();
    }
}