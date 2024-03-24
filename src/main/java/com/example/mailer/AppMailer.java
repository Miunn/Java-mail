package com.example.mailer;

import com.example.mailer.crypto.ElGamal;
import com.example.mailer.mails.Mail;
import com.example.mailer.pkg.PkgHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppMailer extends Application {

    private static Stage myStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppMailer.class.getResource("App.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 700);
        myStage = stage;
        String cssFile = Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm();
        scene.getStylesheets().add(cssFile);

        stage.setTitle("Mail Crypto");
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getMyStage(){
        return myStage;
    }

    public static void main(String[] args) {
        try {
            ElGamal.initCurve();

            Context.CONNECTION_STATE.put("isConnected", "false");
            Context.CONNECTION_STATE.put("email", "");
            Context.CONNECTION_STATE.put("password", "");

            if(Context.connect("eliott.georges8@gmail.com", "jmim ulhb ulxo dfji")) {
                //System.out.println(PkgHandler.register("eliott.georges8@gmail.com"));
                System.out.println(PkgHandler.confirmIdentity());

                //TODO: tant qu'on a pas recu l'email du serveur pkg on attend, puis on récupère le token
                List<Mail> ml = Mail.getMailList( Context.CONNECTION_STATE.get("email"));
                String token = null;
                while(token == null) {
                    for (Mail m : ml) {
                        if (m.getSender().equals("serverpkg@gmail.com")) {
                            token = m.getMessageContent();
                            break;
                        }
                    }
                }
                System.out.println("TOKEN : " + token);
                Context.setChallengeToken("token");
                Context.ELGAMAL_SK = PkgHandler.getSkByValidation();

                if(Context.ELGAMAL_SK == null) {System.exit(1);}

                launch();
            }

        } catch (Exception ex) {
            Logger.getLogger(AppMailer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}