package com.example.mailer;

import com.example.mailer.crypto.ElGamal;
import com.example.mailer.pkg.PkgHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppMailer extends Application {

    private static Stage myStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppMailer.class.getResource("App.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
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

            // eliott.georges8@gmail.com est dans la bdd du PKG
            Context.connect("test", "test");
            System.out.println(PkgHandler.confirmIdentity());
            //Context.ELGAMAL_SK = PkgHandler.getSK();

            launch();

        } catch (Exception ex) {
            Logger.getLogger(AppMailer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}