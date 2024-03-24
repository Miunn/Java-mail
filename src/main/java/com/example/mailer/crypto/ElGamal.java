package com.example.mailer.crypto;

import com.example.mailer.Context;
import com.example.mailer.pkg.PkgHandler;
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

    public Element getPK(String id) {
        if(Context.isConnected()) {
            return PkgHandler.getPK(id);
        } else {
            System.out.println("Erreur: vous n'êtes pas connecté");
        }
        return null;
    }

    public static FileDataSource encryptAttachment(String attachement_path, String fileName, String id) {
        Element pk = new ElGamal().getPK(id);

        if (pairing != null && pk != null) {
            try {
                Element r = pairing.getZr().newRandomElement();
                Element K = pairing.getG1().newElement();
                Element V = pk.duplicate().mulZn(r);
                Element U = generator.duplicate().mulZn(r);
                V.add(K);

                System.out.println("CLE SYMETRIQUE: "+K.toString()); // TODO: probleme: pas la meme cle symetrique que dans le decryptAttachment

                return AesFileCrypt.encryptAttachment(attachement_path, fileName, K.toBytes(), U.toBytes(), V.toBytes());

            } catch (Exception ex) {
                Logger.getLogger(ElGamal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.err.println("Pas de courbe ou de clé publique PK");
        return null;
    }

    public static void decryptAttachment(String attachement_path, String fileName, String destinationPath) {
        if(!Context.isConnected()) {
            System.out.println("Connectez vous pour pouvoir télécharger et déchiffrer les fichiers.");
        } else {
            // Récupération du fichier chiffré
            List<Element> UV = AesFileCrypt.getUV(attachement_path+fileName);
            String originalFileName = fileName.substring(1);  // On enlève le "_" du début du nom de fichier

            if(UV == null) {
                System.out.println("Le fichier ne contient pas de U et V, il est surement non chiffré ou corrompu.\n=> Enregistrement du fichier tel quel dans: "+ destinationPath+originalFileName);
                AesFileCrypt.writeFile(destinationPath+fileName, AesFileCrypt.readFile(attachement_path+fileName));

            } else if(Context.ELGAMAL_SK == null) {
                System.out.println("Erreur: la clé privée SK n'a pas été récupérée");

            } else {
                Element u_p = UV.get(0).duplicate().mulZn(Context.ELGAMAL_SK);
                Element aesKey = UV.get(1).duplicate().sub(u_p); //clef symmetrique AES retrouvée

                System.out.println("CLE SYMETRIQUE RECUP: "+aesKey.toString());

                AesFileCrypt.decryptAttachment(attachement_path, originalFileName, destinationPath, aesKey.toBytes());
            }
        }
    }
}
