package PKG;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        this.generator = p.getG1().newElementFromHash(PKGIdentity.ID.getBytes(), 0, PKGIdentity.ID.length()).getImmutable();
        this.masterKey = p.getZr().newRandomElement().getImmutable();
        this.publicKey = generator.duplicate().mulZn(this.masterKey).getImmutable();
    }

    public Element h1(byte[] id) throws NoSuchAlgorithmException {
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        byte[] idDigest = hash.digest(id);
        return p.getG1().newElementFromHash(idDigest, 0, idDigest.length);
    }

    public void generateKeyPairForClient(Client client) {
        Element Qid;
        try {
            Qid = this.h1(client.getIdentity().getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }

        Element Did = this.masterKey.duplicate().mulZn(Qid);

        client.setPublicKey(Qid);
        client.setPrivateKey(Did);
    }

    public Element getPublicKey() {
        return this.publicKey;
    }
}
