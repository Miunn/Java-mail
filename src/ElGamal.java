import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ElGamal {
    public static Pairing pairing;
    public static Element pk;

    public static void initCurve() {
        pairing = PairingFactory.getPairing(Constants.CURVE);
    }

    public static ElGamalCipherText encryptMail(byte[] data) {
        if (pairing != null && pk != null) {
            try {
                Element r = pairing.getZr().newRandomElement();
                Element K = pairing.getG1().newElement();
                Element V = pk.duplicate().mulZn(r);
                V.add(K);

                byte[] cipherText = AESCrypto.encryptMail(data, K.toBytes());

                return new ElGamalCipherText(K, V, cipherText);

            } catch (Exception ex) {
                Logger.getLogger(ElGamal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static String decryptMail(ElGamalCipherText cipherText, Element sk) { // TODO: return String ???
        try {
            Element u_p = cipherText.u().duplicate().mulZn(sk);

            Element plain = cipherText.v().duplicate().sub(u_p); //clef symmetrique retrouvée

            return AESCrypto.decryptMail(cipherText.cipherText(), plain.toBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | UnsupportedEncodingException ex) {
            Logger.getLogger(ElGamal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
