package PKG;

import java.security.SecureRandom;
import java.util.Base64;
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
    }

    public String getIdentity() {
        return identity;
    }

    public Element getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(Element publicKey) {
        this.publicKey = publicKey;
    }

    public Element getPrivateKey() {
        return this.privateKey;
    }

    public void setPrivateKey(Element privateKey) {
        this.privateKey = privateKey;
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

    public Element[] encodeSkFromPk(String pk_b64) {
        Pairing p = PairingFactory.getPairing("params.properties");

        System.out.println("Call encode sk");
        Element generator = p.getG1().newElementFromHash(this.getIdentity().getBytes(), 0, this.getIdentity().getBytes().length);
        System.out.println("Init generator");


        Element pk = generator.getField().newElementFromBytes(Base64.getDecoder().decode(pk_b64));
        System.out.println("Init pk element");
        Element a = p.getZr().newRandomElement();
        System.out.println("Init a");
        Element U = generator.duplicate().mulZn(a);
        System.out.println("Init U");
        Element V = pk.duplicate().mulZn(a);
        System.out.println("Init V");

        Element M = p.getG1().newElementFromBytes(this.privateKey.toBytes());
        System.out.println("Init M");
        V.add(M);
        System.out.println("Add V");

        System.out.println(U);
        System.out.println(V);
        System.out.println("Sk:" + this.privateKey);

        return new Element[]{U, V};
    }
}