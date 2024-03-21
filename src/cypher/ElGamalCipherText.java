package cypher;

import it.unisa.dia.gas.jpbc.Element;

public record ElGamalCipherText(Element u, Element v, byte[] cipherText) {
}
