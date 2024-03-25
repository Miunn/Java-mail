package com.example.mailer.crypto;

import com.example.mailer.Context;
import com.example.mailer.utils.Constants;
import it.unisa.dia.gas.jpbc.Element;

import java.io.*;
import java.util.Base64;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.FileDataSource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class AesFileCrypt {

    public static byte[] readFile(String filePath) {
        File file = new File(filePath);
        try (InputStream inputStream = new FileInputStream(file)) {
            // Créer un tableau de bytes pour stocker le contenu du fichier
            byte[] buffer = new byte[(int) file.length()];
            // Lire le contenu du fichier dans le buffer
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == -1) {
                throw new IOException("Erreur lors de la lecture du fichier : " + filePath);
            }
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readEncryptedAttachment(String path) {
        File file = new File(path);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Ignorer les quatre premières lignes
            for (int i = 0; i < 4; i++) {
                reader.readLine();
            }
            // Lire le reste du fichier
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            return content.toString().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Element> getUV(String path) {
        try {
            File file = new File(path);
            Scanner sc = new Scanner(file);
            Element U = null;
            Element V = null;

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.equals("#---U---#")) {
                    line = sc.nextLine();
                    U = ElGamal.generator.getField().newElementFromBytes(Base64.getDecoder().decode(line));
                } else if (line.equals("#---V---#")) {
                    line = sc.nextLine();
                    V = ElGamal.generator.getField().newElementFromBytes(Base64.getDecoder().decode(line));
                }
            }
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
            System.err.println("Erreur lors de l'écriture du fichier : " + path);
        }
    }

    public static void writeFile(String filePath, byte[] data) {
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            // Écrire le contenu du buffer dans le fichier
            outputStream.write(data);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier : " + filePath);
        }
    }

    public static void writeEncryptedAttachment(String path, String content, byte[] U, byte[] V) {
        try {
            File file = new File(path);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write("#---U---#\n");
            writer.write(Base64.getEncoder().encodeToString(U));
            writer.write("\n");
            writer.write("#---V---#\n");
            writer.write(Base64.getEncoder().encodeToString(V));
            writer.write("\n");
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
                String encFilePath = Constants.ENC_ATTACHMENTS_PATH+fileName;
                byte[] fileBuffer = readFile(filePath+fileName);

                String encryptedBuffer = AESCrypto.encrypt(fileBuffer, aesKey);
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

    public static void decryptAttachment(String filePath, String fileName, String destinationPath, byte[] aesKey) {
        try {
            File file = new File(filePath+fileName);
            if(file.exists()) {
                byte[] encryptedString = readEncryptedAttachment(filePath+fileName);

                if(encryptedString != null) {
                    String decryptedString = AESCrypto.decrypt(encryptedString, aesKey);
                    writeFile(destinationPath, decryptedString);
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