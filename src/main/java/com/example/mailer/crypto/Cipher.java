package com.example.mailer.crypto;

import com.example.mailer.Context;
import com.example.mailer.pkg.PkgHandler;
import com.example.mailer.utils.Constants;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import javax.activation.FileDataSource;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Cipher {
    public static Pairing pairing;
    public static Element PkgGenerator;

    public static void initCurve() {
        pairing = PairingFactory.getPairing(Constants.CURVE);
    }

    public static void initPkgGenerator(Element P) {
        PkgGenerator = P;
    }

    public static List<Element> getClient(String id) {
        if(Context.isConnected()) {
            return PkgHandler.getClient(id);
        } else {
            System.out.println("Erreur: vous n'êtes pas connecté");
        }
        return null;
    }

    public static byte[] XOR(byte[] a, byte[] b){
        byte[] result = new byte[a.length];
        for(int i = 0; i < a.length; i++){
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    public static FileDataSource encryptAttachment(String attachement_path, String fileName, String id) {
        List<Element> client = Cipher.getClient(id);
        Element Qid = client.get(0);
        Element Kpub = client.get(1);

        if (pairing != null && Kpub != null && PkgGenerator != null) {
            try {
                // r = random in Zp
                Element r = pairing.getZr().newRandomElement().getImmutable();

                // U = r*P
                Element U = PkgGenerator.duplicate().mulZn(r).getImmutable();

                //  -- V = M xor H2(e(Qid, Kpub)^r) --
                // e(Qid, Kpub)
                Element e = pairing.pairing(Qid, Kpub).getImmutable();

                // Generate a random AES key in GT
                byte[] aesKey = pairing.getGT().newRandomElement().getImmutable().toBytes();
                System.out.println("Clé Aes générée: "+ Arrays.toString(aesKey));

                // e(Qid, Kpub)^r
                byte[] V = XOR(aesKey, e.powZn(r).toBytes());

                return AesFileCrypt.encryptAttachment(attachement_path, fileName, aesKey, U.toBytes(), V);

            } catch (Exception ex) {
                Logger.getLogger(Cipher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.err.println("Pas de courbe, ou de clé publique PK, ou de générateur PkgGenerator.");
        return null;
    }

    public static void decryptAttachment(String attachement_path, String fileName, String destinationPath) {
        if(!Context.isConnected()) {
            System.out.println("Connectez vous pour pouvoir télécharger et déchiffrer les fichiers.");

        } else {
            // Récupération du fichier chiffré
            List<Element> UV = AesFileCrypt.getUV(attachement_path+fileName);

            if(UV == null) {
                String originalFileName = fileName.substring(1);  // On enlève le "_" du début du nom de fichier
                System.out.println("Le fichier ne contient pas de U et V, il est surement non chiffré ou corrompu.\n=> Enregistrement du fichier tel quel dans: "+ destinationPath+originalFileName);
                AesFileCrypt.writeFile(destinationPath+originalFileName, AesFileCrypt.readFile(attachement_path+fileName));

            } else if(Context.Did == null) {
                System.out.println("Erreur: la clé privée SK n'a pas été récupérée");

            } else {
                Element U = UV.get(0);
                Element V = UV.get(1);

                // e(Did, U)
                Element e = pairing.pairing(Context.Did, U).getImmutable();

                // AES key = V XOR H2(e(Q_id, publicKey)^r)
                byte[] aesKey = XOR(V.toBytes(), e.toBytes());

                System.out.println("Clé Aes récupérée: "+ Arrays.toString(aesKey));

                AesFileCrypt.decryptAttachment(attachement_path, fileName, destinationPath, aesKey);
            }
        }
    }
}
