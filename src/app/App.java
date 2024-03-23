package app;

import PKG.PkgHandler;
import cypher.ElGamal;

import java.util.logging.Level;
import java.util.logging.Logger;


public class App {

    public static void main(String[] args) {
        try {
            ElGamal.initCurve();

            Context.CONNECTION_STATE.put("isConnected", "false");
            Context.CONNECTION_STATE.put("email", "");
            Context.CONNECTION_STATE.put("password", "");

            Context.connect("email", "password");
            Context.setChallengeToken(PkgHandler.confirmIdentity());
            Context.ELGAMAL_SK = PkgHandler.getSK();

        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
