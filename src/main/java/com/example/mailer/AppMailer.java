package com.example.mailer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppMailer extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppMailer.class.getResource("test.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);

        String cssFile = getClass().getResource("style.css").toExternalForm();
        scene.getStylesheets().add(cssFile);

        stage.setTitle("Mail Crypto");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}