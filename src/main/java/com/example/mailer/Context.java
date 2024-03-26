package com.example.mailer;

import com.example.mailer.utils.Constants;
import it.unisa.dia.gas.jpbc.Element;

import javax.mail.*;
import java.util.HashMap;
import java.util.Properties;

public class Context {
    public static HashMap<String, String> CONNECTION_STATE = new HashMap<>();
    public static Session EMAIL_SESSION = null;
    public static Store STORE = null;
    public static Element SK = null;
    public static Element PKG_PK = null;
    public static String CHALLENGE_TOKEN = null;


    public static boolean isConnected() {
        System.out.println(CONNECTION_STATE.get("isConnected").equals("true"));
        return CONNECTION_STATE.get("isConnected").equals("true");
    }

    public static boolean connect(String email, String password) {
        try {
            Properties properties = new Properties();

            properties.put("mail.smtp.host", Constants.MAIL_HOST);
            properties.put("mail.smtp.port", Constants.SMTP_PORT);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");

            EMAIL_SESSION = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(email, password);
                }
            });

            properties.put("mail.imap.host", Constants.MAIL_HOST);
            properties.put("mail.imap.port", Constants.IMAP_PORT);
            properties.setProperty("mail.imap.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.imap.socketFactory.fallback", "false");
            properties.setProperty("mail.imap.socketFactory.port", Constants.IMAP_PORT);

            Session session = Session.getDefaultInstance(properties);

            STORE = session.getStore("imap");

            STORE.connect(email, password);


            CONNECTION_STATE.put("isConnected", "true");
            CONNECTION_STATE.put("email", email);
            CONNECTION_STATE.put("password", password);

            return true;
        } catch (MessagingException e) {
            System.out.println("Erreur lors de la connexion au compte");
            System.exit(1);
        }

        return false;
    }

    public static void setChallengeToken(String token) {
        CHALLENGE_TOKEN = token;
    }

    public static void dispose() throws MessagingException {
        STORE.close();
    }
}
