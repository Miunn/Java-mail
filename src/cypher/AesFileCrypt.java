package cypher;

import app.Context;
import it.unisa.dia.gas.jpbc.Element;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class AesFileCrypt {

    public static String readFile(String path) {
        try {
            File file = new File(path);
            Scanner sc = new Scanner(file);
            StringBuilder buffer = new StringBuilder();
            while (sc.hasNextLine()) {
                buffer.append(sc.nextLine());
                buffer.append("\n");
            }
            sc.close();
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String readEncryptedAttachment(String path) {
        try {
            File file = new File(path);
            Scanner sc = new Scanner(file);
            StringBuilder buffer = new StringBuilder();
            int i = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                i++;
                if (i > 4) {  // Les 4 premières lignes sont les clés U et V
                    buffer.append(line);
                    buffer.append("\n");
                }
            }
            sc.close();
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Element> getUV(String path) {
        try {
            File file = new File(path);
            Scanner sc = new Scanner(file);
            Element U = null; // TODO: recupérer U depuis le fichier: première ligne après #---U---#
            Element V = null; // TODO: recupérer V depuis le fichier: première ligne après #---V---#
            sc.close();

            if (U != null && V != null) {
                return List.of(U, V);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void writeFile(String path, String content) {
        try {
            File file = new File(path);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeEncryptedAttachment(String path, String content, byte[] U, byte[] V) {
        try {
            File file = new File(path);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write("#---U---#\n");
            writer.write(new String(U));
            writer.write("#---V---#\n");
            writer.write(new String(V));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileDataSource encryptAttachment(String filePath, String fileName, byte[] aesKey, byte[] U, byte[] V) {
        try {
            File file = new File(filePath+fileName);
            if(file.exists()) {
                String encFilePath = "/tmp/enc_"+fileName+".txt";
                String fileBuffer = readFile(filePath+fileName);
                if(fileBuffer == null) {
                    System.out.println("Erreur de lecture du fichier");
                    return null;
                }

                String encryptedBuffer = new String(AESCrypto.encrypt(fileBuffer.getBytes(), aesKey)) ;
                writeEncryptedAttachment(encFilePath, encryptedBuffer, U, V);

                return new FileDataSource(encFilePath);

            } else {
                System.out.println("Le fichier n'existe pas");
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | UnsupportedEncodingException ex) {
            Logger.getLogger(AesFileCrypt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void decryptAttachment(String filePath, String fileName, byte[] aesKey) {
        try {
            File file = new File(filePath+fileName);
            if(file.exists()) {
                String newFilePath = "/Myfiles"+fileName+".txt";
                String encryptedString = readEncryptedAttachment(filePath+fileName);

                if(encryptedString != null) {
                    String decryptedString = AESCrypto.decrypt(encryptedString.getBytes(StandardCharsets.UTF_8), aesKey);
                    writeFile(newFilePath, decryptedString);
                } else {
                    System.out.println("Erreur de lecture du fichier");
                }
            } else {
                System.out.println("Le fichier n'existe pas");
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | UnsupportedEncodingException ex) {
            Logger.getLogger(AesFileCrypt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}