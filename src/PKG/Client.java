package PKG;

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

    Client(String identity) {
        this.identity = identity;
        this.generateKeyPair();
    }

    public String getidentity() {
        return identity;
    }

    public Element getPublicKey() {
        return this.publicKey;
    }

    public Element getPrivateKey() {
        return this.privateKey;
    }

    public JsonObject getClientPublicJSON() {
        return Json.createObjectBuilder()
                    .add("identity", this.identity)
                    .add("pk", Base64.getEncoder().encodeToString(this.publicKey.toBytes()))
                    .build();
    }

    private void generateKeyPair() {
        Pairing p = PairingFactory.getPairing("params.properties");
        Element generator = p.getG1().newRandomElement();
        this.privateKey = p.getZr().newRandomElement();

        this.publicKey = generator.duplicate().mulZn(this.privateKey);
    }
}