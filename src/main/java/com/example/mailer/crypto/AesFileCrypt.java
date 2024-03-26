package com.example.mailer.crypto;

import com.example.mailer.utils.Constants;
import it.unisa.dia.gas.jpbc.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;
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


    public static byte[] readFile(String path) {
        File file = new File(path);
        byte[] buffer = new byte[(int) file.length()];
        try(FileInputStream fis = new FileInputStream(file)) {
            fis.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return buffer;
    }

    public static byte[] readEncryptedAttachment(String path) {
        try {
            File file = new File(path);
            Scanner sc = new Scanner(file);
            StringBuilder buffer = new StringBuilder();
            int i = 0;
            while (sc.hasNextLine()) {
                if(i < 4) {
                    i++;
                    sc.nextLine();
                } else {
                    buffer.append(sc.nextLine());
                }
            }
            sc.close();

            return buffer.toString().getBytes();

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
                    U = Cipher.generator.getField().newElementFromBytes(Base64.getDecoder().decode(line));
                } else if (line.equals("#---V---#")) {
                    line = sc.nextLine();
                    V = Cipher.generator.getField().newElementFromBytes(Base64.getDecoder().decode(line));
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

    public static void writeFile(String path, byte[] buffer) {
        File file = new File(path);
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(buffer);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier : " + e);
        }
    }

    public static void writeEncryptedAttachment(String path, byte[] content, byte[] U, byte[] V) {
        try (FileOutputStream outputStream = new FileOutputStream(path)) {

            outputStream.write("#---U---#\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write(Base64.getEncoder().encodeToString(U).getBytes(StandardCharsets.UTF_8));
            outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write("#---V---#\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write(Base64.getEncoder().encodeToString(V).getBytes(StandardCharsets.UTF_8));
            outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write(content);

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

                byte[] encryptedBuffer = AESCrypto.encrypt(fileBuffer, aesKey);
                writeEncryptedAttachment(encFilePath, encryptedBuffer, U, V);

                return new FileDataSource(encFilePath);

            } else {
                System.err.println("Le fichier n'existe pas (encryptAttachment)");
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
                    byte[] decryptedString = AESCrypto.decrypt(encryptedString, aesKey);

                    String originalFileName = fileName.substring(1);  // On enlève le "_" du début du nom de fichier
                    writeFile(destinationPath+originalFileName, decryptedString);
                } else {
                    System.err.println("Erreur de lecture du fichier chiffré (decryptAttachment)");
                }
            } else {
                System.err.println("Le fichier '"+filePath+fileName+"' n'existe pas (decryptAttachment)");
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | UnsupportedEncodingException ex) {
            Logger.getLogger(AesFileCrypt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}