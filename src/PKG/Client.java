package PKG;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.json.Json;
import javax.json.JsonObject;

public class Client {

    private String identity;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    Client(String identity) {
        this.identity = identity;
        this.generateKeyPair();
    }

    public String getidentity() {
        return identity;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public JsonObject getClientPublicJSON() {
        return Json.createObjectBuilder()
                    .add("identity", this.identity)
                    .add("pk", Base64.getEncoder().encodeToString(this.publicKey.getEncoded()))
                    .build();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("ElGamal", "BC");
            keyPairGen.initialize(128);
            KeyPair keyPair = keyPairGen.genKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException|NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
}