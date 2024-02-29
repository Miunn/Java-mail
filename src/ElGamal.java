import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ElGamal {

    public ElGamalCipherText encrypt(Pairing pairing, Element g, Element h, Element pk) {
        try {
            Element r = pairing.getZr().newRandomElement();
            Element K = pairing.getG1().newElement();
            Element V = g.duplicate().mulZn(r);
            V.add(K);

            byte[] cipherText = ... // TODO : RSA ???

            return new ElGamalCipherText(K, V, cipherText);

        } catch (Exception ex) {
            Logger.getLogger(ElGamal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
