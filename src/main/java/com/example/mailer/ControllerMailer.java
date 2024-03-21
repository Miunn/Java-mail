package com.example.mailer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


import java.util.List;

public class ControllerMailer implements Initializable {
    @FXML
    private VBox newMsgVbox;
    @FXML
    private VBox openMsgVbox;
    @FXML
    private VBox mailList;
    @FXML
    private Label titreMessage;
    @FXML
    private Label senderMessage;
    @FXML
    private Label msgMessage;
    @FXML
    private Button btnPJ;
    @FXML
    private HBox fileContainer;
    @FXML
    private Label fileName;
    private File PJ;

    private List<Mail> mails = new ArrayList<>();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remplir la liste des courriers électroniques
        mails.add(new Mail("Mise a jour des informations","Jonh Newman", "Une interface web est une interface homme-machine constituée de pages web et permettant dans certains cas d'utiliser des applications web.\n" +
                "\n" +
                "Un client ou plus populairement un navigateur web tel que Firefox, Safari, Google Chrome, Internet Explorer ou bien Opera étant installable et généralement présent sur tout ordinateur moderne, une interface web est visualisable à partir de n'importe quel dispositif possédant un navigateur web (ordinateur, tablette ou smartphone, etc.). Elle est aussi potentiellement accessible du monde entier grâce à l'Internet.\n" +
                "\n" +
                "Pour choisir le contenu de la page du navigateur il faut entrer une URL.\n" +
                "\n" +
                "De nombreux matériels tels que routeur, modem ou photocopieur disposent d'une interface web permettant de les administrer."));
        for(int i=0; i<30;i++) {
            mails.add(new Mail("titre" + i,"sender","cocuouc"));
        }
        for (Mail mail : mails) {
            VBox mailBox = createMailBox(mail);
            mailBox.getStyleClass().add("mailBox");
            mailList.getChildren().add(mailBox);
        }

        btnPJ.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir un fichier");
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            String filename;
            if (selectedFile != null) {
                PJ = selectedFile;
                filename = selectedFile.getName();
                String extension = "";
                int lastIndexOfDot = filename.lastIndexOf(".");
                if (lastIndexOfDot != -1) {
                    extension = filename.substring(lastIndexOfDot + 1);
                }
            }
            if(PJ != null){
                filename = PJ.getName();
                fileContainer.setVisible(true);
                fileName.setText(filename);
            }
        });

    }

    private VBox createMailBox(Mail mail) {
        VBox mailBox = new VBox(3);
        Label titleLabel = new Label(mail.getTitle());
        Label senderLabel = new Label(mail.getSender());
        Label messageLabel = new Label(mail.getMessage());

        titleLabel.getStyleClass().add("mailBox-subject");
        senderLabel.getStyleClass().add("mailBox-sender");
        messageLabel.getStyleClass().add("mailBox-message");

        mailBox.setOnMouseClicked(event -> {
            openMessage(mail);
        });
        mailBox.getChildren().addAll(senderLabel,titleLabel, messageLabel);
        return mailBox;
    }

    @FXML
    protected void newMessage() {
        if(!newMsgVbox.isVisible()){
            newMsgVbox.setVisible(!newMsgVbox.isVisible());
            newMsgVbox.setManaged(newMsgVbox.isVisible());
            openMsgVbox.setVisible(!openMsgVbox.isVisible());
            openMsgVbox.setManaged(openMsgVbox.isVisible());
        }
    }

    @FXML
    protected void openMessage(Mail mail) {
        if(newMsgVbox.isVisible()){ //Affichage interface message recu + maj infos
            newMsgVbox.setVisible(!newMsgVbox.isVisible());
            newMsgVbox.setManaged(newMsgVbox.isVisible());
            openMsgVbox.setVisible(!openMsgVbox.isVisible());
            openMsgVbox.setManaged(openMsgVbox.isVisible());
            titreMessage.setText(mail.getTitle());
            senderMessage.setText(mail.getSender());
            msgMessage.setText(mail.getMessage());
        }else{ //maj infos
            titreMessage.setText(mail.getTitle());
            senderMessage.setText(mail.getSender());
            msgMessage.setText(mail.getMessage());
        }
    }
}