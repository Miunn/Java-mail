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
    private Label welcomeText;
    @FXML
    private VBox newMsgVbox;
    @FXML
    private VBox vbox3;

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
            mailList.getChildren().add(mailBox);
        }
    }

    private VBox createMailBox(Mail mail) {
        VBox mailBox = new VBox(5);
        Label titleLabel = new Label(mail.getTitle());
        Label senderLabel = new Label("Expéditeur: " + mail.getSender());
        Label messageLabel = new Label(mail.getMessage());
        mailBox.getChildren().addAll(titleLabel, senderLabel, messageLabel);
        return mailBox;
    }

    // New Mail Button logic
    @FXML
    protected void onNewMsgBtnClick() {
        newMsgVbox.setVisible(!newMsgVbox.isVisible());
        newMsgVbox.setManaged(newMsgVbox.isVisible());
        vbox3.setVisible(!vbox3.isVisible());
        vbox3.setManaged(vbox3.isVisible());
    }
}