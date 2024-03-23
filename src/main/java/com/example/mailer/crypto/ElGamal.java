package com.example.mailer.crypto;

import com.example.mailer.Context;
import com.example.mailer.utils.Constants;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import javax.activation.FileDataSource;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ElGamal {
    public static Pairing pairing;
    public static Element generator;

    public static void initCurve() {
        pairing = PairingFactory.getPairing(Constants.CURVE);
        generator = pairing.getG1().newRandomElement();
    }

    public Element getPK() { // TODO: on la récupère en interrogeant le serveur ou direct dans le mail ? (ce que le prof avait proposé)
        return null;
    }

    public static FileDataSource encryptAttachment(String attachement_path, String fileName) {
        Element pk = new ElGamal().getPK();

        if (pairing != null && pk != null) {
            try {
                Element r = pairing.getZr().newRandomElement();
                Element K = pairing.getG1().newElement();
                Element V = pk.duplicate().mulZn(r);
                Element U=generator.duplicate().mulZn(r);
                V.add(K);

                return AesFileCrypt.encryptAttachment(attachement_path, fileName, K.toBytes(), U.toBytes(), V.toBytes());

            } catch (Exception ex) {
                Logger.getLogger(ElGamal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static void decryptAttachment(String attachement_path, String fileName) {
        if(!Context.isConnected() || Context.ELGAMAL_SK == null) {
            System.out.println("Veuillez vous connecter pour récupérer votre clé privée");
        } else {
            // Récupération du fichier chiffré
            List<Element> UV = AesFileCrypt.getUV(attachement_path+fileName);

            Element u_p = UV.get(0).duplicate().mulZn(Context.ELGAMAL_SK);
            Element aesKey = UV.get(1).duplicate().sub(u_p); //clef symmetrique AES retrouvée

            AesFileCrypt.decryptAttachment(attachement_path, fileName, aesKey.toBytes());
        }
    }
}
