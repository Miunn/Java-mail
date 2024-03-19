import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    public static void main(String[] args)
    {
        // TODO: à adapter
        try {
            final String secretKey = "voici la clé de secrète!!!!";
            Scanner sc=new Scanner(System.in);
            System.out.println("Entrez le nom du fichier à chiffrer:");
            String fileName = sc.nextLine();

            File file = new File(fileName);
            if(file.exists())
            {
                String originalString = readFile(fileName);
                assert originalString != null;
                String encryptedString = new String(AESCrypto.encrypt(originalString.getBytes(), secretKey.getBytes())) ;
                writeFile("file.txt", encryptedString);
                String decryptedString = AESCrypto.decrypt(encryptedString.getBytes(StandardCharsets.UTF_8), secretKey.getBytes()) ;

                System.out.println(originalString);
                System.out.println(encryptedString);
                System.out.println(decryptedString);
            }
            else
            {
                System.out.println("Le fichier n'existe pas");
            }


        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | UnsupportedEncodingException ex) {
            Logger.getLogger(AesFileCrypt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}