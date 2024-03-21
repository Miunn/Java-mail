package app;

import it.unisa.dia.gas.jpbc.Element;

public class Context {
    public static Boolean CONNECTED = false;
    public static String ID = "";
    public static Element ELGAMAL_SK = null;


    public static void connect(String id) {
        ID = id;
        CONNECTED = true;
    }
}
