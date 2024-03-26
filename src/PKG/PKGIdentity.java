package PKG;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class PKGIdentity {

    public static String ID = "serveurpkg@gmail.com";

    private Pairing p = PairingFactory.getPairing("params.properties");
    private Element generator;
    private Element masterKey;
    private Element publicKey;

    public PKGIdentity() {
        this.generator = p.getG1().newElementFromHash(PKGIdentity.ID.getBytes(), 0, PKGIdentity.ID.length())
                .getImmutable();
        this.masterKey = p.getZr().newRandomElement().getImmutable();
        this.publicKey = generator.duplicate().mulZn(this.masterKey).getImmutable();
    }

    public Element getP() {
        return this.generator;
    }

    public Element h1(byte[] id) throws NoSuchAlgorithmException {
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        byte[] idDigest = hash.digest(id);
        return p.getG1().newElementFromHash(idDigest, 0, idDigest.length);
    }

    public void generateKeyPairForClient(Client client) {
        System.out.println("Generate key pair for client:"+client.getIdentity());
        Element Qid;
        try {
            Qid = this.h1(client.getIdentity().getBytes()).getImmutable();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Got Qid:"+Qid.toString());

        Element Did = this.masterKey.duplicate().mulZn(Qid);
        System.out.println("Got Did:"+Did.toString());

        client.setPublicKey(Qid);
        client.setPrivateKey(Did);
    }

    public Element getPublicKey() {
        return this.publicKey;
    }

    public JsonObject getClientPublicJSON(Client client) {
        JsonObject json = Json.createObjectBuilder()
                .add("identity", client.getIdentity())
                .add("P", Base64.getEncoder().encodeToString(this.generator.toBytes()))
                .add("Kpub", Base64.getEncoder().encodeToString(this.publicKey.toBytes()))
                .add("Qid", Base64.getEncoder().encodeToString(client.getPublicKey().toBytes()))
                .build();

        System.out.println(json.toString());

        return json;
    }
}
