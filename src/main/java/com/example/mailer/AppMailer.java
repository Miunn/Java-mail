package com.example.mailer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

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
        launch();
    }
}