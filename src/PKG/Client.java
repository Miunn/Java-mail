package PKG;

import java.security.SecureRandom;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class Client {

    private String identity;
    private Element privateKey;
    private Element publicKey;
    private String challengeCode;

    Client(String identity) {
        this.identity = identity;
        this.generateKeyPair();
    }

    public String getIdentity() {
        return identity;
    }

    public Element getPublicKey() {
        return this.publicKey;
    }

    public Element getPrivateKey() {
        return this.privateKey;
    }

    public void generateChallengeCode() {
        SecureRandom secureRandom = new SecureRandom();

        byte[] codeVerifier = new byte[64];
        secureRandom.nextBytes(codeVerifier);
        this.challengeCode = Base64.getEncoder().encodeToString(codeVerifier);
    }

    public boolean validateChallenge(String code) {
        boolean r = this.challengeCode.equals(code);

        if (r) {
            this.challengeCode = null;
        }

        return r;
    }

    public String getToken() {
        return this.challengeCode;
    }

    public JsonObject getClientPublicJSON() {
        return Json.createObjectBuilder()
                    .add("identity", this.identity)
                    .add("pk", Base64.getEncoder().encodeToString(this.publicKey.toBytes()))
                    .build();
    }

    private void generateKeyPair() {
        Pairing p = PairingFactory.getPairing("params.properties");

        Element generator = p.getG1().newElementFromHash(this.identity.getBytes(), 0, this.identity.getBytes().length);
        this.privateKey = p.getZr().newRandomElement();

        this.publicKey = generator.duplicate().mulZn(this.privateKey);
    }
}