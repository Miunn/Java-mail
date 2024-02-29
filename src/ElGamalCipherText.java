import it.unisa.dia.gas.jpbc.Element;

public class ElGamalCipherText {
    public Element u;
    public Element v;
    public byte[] cipherText;

    public ElGamalCipherText(Element u, Element v, byte[] cipherText) {
        this.u = u;
        this.v = v;
        this.cipherText = cipherText;
    }
}
