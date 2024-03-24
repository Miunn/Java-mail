package com.example.mailer.utils;

public class Constants {

    public static int DISPLAY_NB = 20;

    public static final String PKG_HOST = "https://pkg.remicaulier.fr/";
    public static final String SK_ENDPOINT = "/sk";
    public static final String PK_ENDPOINT = "/get";
    public static final String CHALLENGE_ENDPOINT = "/challenge";
    public static final String VALIDATE_ENDPOINT = "/validate";
    public static final String REGISTER_ENDPOINT = "/register";


    public static final String MAIL_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "587";
    public static final String IMAP_PORT = "993";
    public static final String ENC_ATTACHMENTS_PATH = "tmp/";


    public static final String AES = "AES";
    public static final String AES_Padding = "AES/ECB/PKCS5Padding";
    public static final String Digest_Alg = "SHA1";
    public static final String CURVE = "src/main/java/com/example/mailer/crypto/curves/param.properties";

}
