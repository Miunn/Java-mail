package com.example.mailer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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

    private List<Mail> mails = new ArrayList<>();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remplir la liste des courriers électroniques
        for(int i=0; i<30;i++) {
            mails.add(new Mail("titre" + i,"sender","cocuouc"));
        }
        for (Mail mail : mails) {
            VBox mailBox = createMailBox(mail);
            mailBox.getStyleClass().add("mailBox");
            mailList.getChildren().add(mailBox);

        }
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
        }else{ //maj infos

        }
    }
}