package app;

import it.unisa.dia.gas.jpbc.Element;

public class Context {
    public static Boolean CONNECTED = false;
    public static String ID = "";
    public static Element ELGAMAL_SK = null;
    public static String CHALLENGE_TOKEN = "";


    public static void connect(String id) {
        ID = id;
        CONNECTED = true;
    }

    public static void setChallengeToken(String token) {
        CHALLENGE_TOKEN = token;
    }
}
