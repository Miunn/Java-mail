package app;

import it.unisa.dia.gas.jpbc.Element;
import utils.Constants;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class Context {
    public static HashMap<String, String> CONNECTION_STATE = new HashMap<>();
    public static Session EMAIL_SESSION = null;
    public static Element ELGAMAL_SK = null;
    public static String CHALLENGE_TOKEN = "";


    public static boolean isConnected() {
        return CONNECTION_STATE.get("isConnected").equals("true");
    }

    public static void connect(String email, String password) {
        Properties properties = new Properties();

        properties.put("mail.smtp.host", Constants.MAIL_HOST);
        properties.put("mail.smtp.port", Constants.SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // TODO: comment gerer le cas ou il y a une erreur de connexion
        EMAIL_SESSION = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
        CONNECTION_STATE.put(CONNECTION_STATE.get("isConnected"), "true");
        CONNECTION_STATE.put(CONNECTION_STATE.get("email"), email);
        CONNECTION_STATE.put(CONNECTION_STATE.get("password"), password);
    }

    public static void setChallengeToken(String token) {
        CHALLENGE_TOKEN = token;
    }
}
