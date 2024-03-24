package com.example.mailer;

import com.example.mailer.mails.Mail;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.List;
import javafx.scene.web.WebView;


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
    private VBox filesContainer;
    @FXML
    private VBox attachmentsContainer;
    @FXML
    private Label fileName;
    @FXML
    private TextField newMsgDest;
    @FXML
    private TextField newMsgTitle;
    @FXML
    private TextArea newMsgMessage;
    @FXML
    private VBox objRespContainer;
    @FXML
    private Label objResp;
    @FXML
    private WebView msgContent;

    private List<File> Pjs = new ArrayList<>();
    private List<String> PJsNames = new ArrayList<>();
    //private File PJ;
    private List<Mail> mails = new ArrayList<>();

    private Stage primaryStage = AppMailer.getMyStage();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mails = Mail.getMailList(Context.CONNECTION_STATE.get("email"));

        for (Mail mail : mails) {
            VBox mailBox = createMailBox(mail);
            mailBox.getStyleClass().add("mailBox");
            mailList.getChildren().add(mailBox);
        }

        btnPJ.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir un fichier");
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile != null) {
                addPJ(selectedFile);
                filesContainer.setVisible(true);
                filesContainer.setManaged(true);
            }
        });
    }

    private void addPJ(File pj){
        Pjs.add(pj);
        PJsNames.add(pj.getName());

        HBox fileContainer = new HBox();
        HBox.setHgrow(fileContainer, Priority.ALWAYS);

        VBox fileIconVBox = new VBox();
        fileIconVBox.getStyleClass().add("fileBloc");
        ImageView fileIconImageView = new ImageView();
        fileIconImageView.setFitWidth(35);
        fileIconImageView.setPreserveRatio(true);
        fileIconImageView.setImage(new Image(Objects.requireNonNull(getClass().getResource("/Images/file.png")).toString()));
        fileIconVBox.getChildren().add(fileIconImageView);

        HBox fileNameHBox = new HBox();
        HBox.setHgrow(fileNameHBox, Priority.ALWAYS);
        fileNameHBox.setAlignment(Pos.CENTER_LEFT);
        fileNameHBox.getStyleClass().add("fileNameBloc");
        Label fileNameLabel = new Label(pj.getName());
        VBox.setMargin(fileNameLabel, new javafx.geometry.Insets(0, 15, 0, 0));
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        ImageView delFileImageView = new ImageView();
        delFileImageView.setFitWidth(23);
        delFileImageView.setPreserveRatio(true);
        delFileImageView.setImage(new Image(Objects.requireNonNull(getClass().getResource("/Images/close.png")).toString()));
        delFileImageView.setOnMouseClicked(e -> delPJ(Pjs.indexOf(pj)));
        VBox.setMargin(delFileImageView, new javafx.geometry.Insets(0, 15, 0, 0));

        fileNameHBox.getChildren().addAll(fileNameLabel, region, delFileImageView);
        fileContainer.getChildren().addAll(fileIconVBox, fileNameHBox);
        filesContainer.getChildren().add(fileContainer);
    }
    private void addPJFromMail(String namePJ){
        HBox fileContainer = new HBox();
        HBox.setHgrow(fileContainer, Priority.ALWAYS);

        VBox fileIconVBox = new VBox();
        fileIconVBox.getStyleClass().add("fileBloc");
        ImageView fileIconImageView = new ImageView();
        fileIconImageView.setFitWidth(35);
        fileIconImageView.setPreserveRatio(true);
        fileIconImageView.setImage(new Image(Objects.requireNonNull(getClass().getResource("/Images/file.png")).toString()));
        fileIconVBox.getChildren().add(fileIconImageView);

        HBox fileNameHBox = new HBox();
        HBox.setHgrow(fileNameHBox, Priority.ALWAYS);
        fileNameHBox.setAlignment(Pos.CENTER_LEFT);
        fileNameHBox.getStyleClass().add("fileNameBloc");
        Label fileNameLabel = new Label(namePJ);
        VBox.setMargin(fileNameLabel, new javafx.geometry.Insets(0, 15, 0, 0));
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        ImageView delFileImageView = new ImageView();
        delFileImageView.setFitWidth(23);
        delFileImageView.setPreserveRatio(true);
        delFileImageView.setImage(new Image(Objects.requireNonNull(getClass().getResource("/Images/close.png")).toString()));
        delFileImageView.setOnMouseClicked(e -> delPJ(PJsNames.indexOf(namePJ)));
        VBox.setMargin(delFileImageView, new javafx.geometry.Insets(0, 15, 0, 0));

        fileNameHBox.getChildren().addAll(fileNameLabel, region, delFileImageView);
        fileContainer.getChildren().addAll(fileIconVBox, fileNameHBox);
        filesContainer.getChildren().add(fileContainer);
    }


    private VBox createMailBox(Mail mail) {
        VBox mailBox = new VBox(5);
        Label titleLabel = new Label(mail.getObject());
        Label senderLabel = new Label(mail.getSender());
        //Label messageLabel = new Label(extractTextFromHtml(mail.getMessageContent()));

        if(mail.getSeen()){
            senderLabel.getStyleClass().add("mailBox-sender-seen");
        }else{
            senderLabel.getStyleClass().add("mailBox-sender");
        }
        titleLabel.getStyleClass().add("mailBox-subject-seen");

        if(!mail.getAttachements().isEmpty()){
            HBox hbox = new HBox(3);
            HBox.setHgrow(hbox, Priority.ALWAYS);
            Region region = new Region();
            HBox.setHgrow(region, Priority.ALWAYS);
            hbox.setAlignment(Pos.CENTER);
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/pj.png")));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(15);
            imageView.setPickOnBounds(true);
            imageView.setPreserveRatio(true);
            hbox.getChildren().addAll(senderLabel,region,imageView);
            mailBox.getChildren().addAll(hbox,titleLabel);
        }else{
            mailBox.getChildren().addAll(senderLabel,titleLabel);
        }

        mailBox.setOnMouseClicked(event -> {
            openMessage(mail);
        });

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
        for (Mail m :mails) {
            m.setActif(false);
        }
        refreshMailList();
        newMsgDest.setText("");
        newMsgTitle.setText("");
        setHtmlContent(false,"");
        setNotHtmlContent(false, "");
        setTextarea(false,"");
        delPJ();
    }

    private void setDl(Mail mail){
        if(!mail.getAttachements().isEmpty()) {
            for (String attachmentName : mail.getAttachements()) {
                PJsNames.add(attachmentName);

                HBox attachmentBox = new HBox();
                attachmentBox.getStyleClass().add("blocDlFile");
                attachmentBox.setAlignment(Pos.CENTER_LEFT);

                VBox fileIconVBox = new VBox();
                fileIconVBox.getStyleClass().add("fileBloc");
                ImageView fileIcon = new ImageView();
                fileIcon.setFitWidth(35);
                fileIcon.setPreserveRatio(true);
                fileIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/Images/file.png")).toString()));
                fileIconVBox.getChildren().add(fileIcon);

                HBox fileNameHBox = new HBox();
                HBox.setHgrow(fileNameHBox, Priority.ALWAYS);
                fileNameHBox.setAlignment(Pos.CENTER_LEFT);
                fileNameHBox.getStyleClass().add("fileNameBloc");
                Label attachmentLabel = new Label(attachmentName);
                attachmentLabel.setWrapText(true);
                VBox.setMargin(attachmentLabel, new javafx.geometry.Insets(0, 0, 0, 15));
                Region region = new Region();
                HBox.setHgrow(region, Priority.ALWAYS);
                ImageView downloadIcon = new ImageView();
                downloadIcon.setFitWidth(23);
                downloadIcon.setPreserveRatio(true);
                downloadIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/Images/dl.png")).toString()));
                downloadIcon.setOnMouseClicked(event -> {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Choisir un dossier");
                    // Afficher la boîte de dialogue de sélection de dossier
                    File selectedDirectory = directoryChooser.showDialog(primaryStage);
                    if (selectedDirectory != null) {
                        String destinationPath = selectedDirectory.getAbsolutePath();

                        // Déchiffrement de la pièce jointe + sauvegarde
                        mail.downloadEmailAttachments(destinationPath);
                    }
                });
                VBox.setMargin(downloadIcon, new javafx.geometry.Insets(0, 15, 0, 0));
                fileNameHBox.getChildren().addAll(attachmentLabel,region,downloadIcon);

                attachmentBox.getChildren().addAll(fileIconVBox, fileNameHBox);
                attachmentsContainer.getChildren().add(attachmentBox);
            }
            attachmentsContainer.setVisible(true);
            attachmentsContainer.setManaged(true);
        }

    }



    private void openMessage(Mail mail) {
        newMsgVbox.setUserData(mails.indexOf(mail));
        if(newMsgVbox.isVisible()){ //Affichage interface message recu + maj infos
            newMsgVbox.setVisible(false);
            newMsgVbox.setManaged(false);
            openMsgVbox.setVisible(true);
            openMsgVbox.setManaged(true);
        }
        delPJ();
        titreMessage.setText(mail.getObject());
        senderMessage.setText(mail.getSender());
        String mailContent = mail.getMessageContent();
        if(isHtmlContent(mailContent)){
            setHtmlContent(true,"");
            setHtmlContent(true, mail.getMessageContent());
            setNotHtmlContent(false, "");
        }else{
            setHtmlContent(false, "");
            setNotHtmlContent(true, mail.getMessageContent());
        }
        if(!mail.getAttachements().isEmpty()){
            setDl(mail);
        }else{
            attachmentsContainer.setVisible(false);
            attachmentsContainer.setManaged(false);

        }
        if(!mail.getSeen()){
            mail.setSeen(true);
        }
        for (Mail m :mails) {
            m.setActif(false);
        }
        mail.setActif(true);
        refreshMailList();
    }

    private void openAutoMessage(int index) {
        Mail mail = mails.get(index);
        openMessage(mail);
    }

    @FXML
    protected void deleteMessage(ActionEvent event) {
        int index = (int)newMsgVbox.getUserData();
        if (index >= 0 && index < mails.size()) {
            mails.remove(index);
            refreshMailList();
            if((index) < mails.size()){
                openAutoMessage(index);
            } else if (index==mails.size() && index!=0){
                openAutoMessage(index-1);
            } else {
                newMessage();
            }
        } else {
            System.err.println("Index invalide : " + index );

        }
    }

    private void refreshMailList() {
        mailList.getChildren().clear();
        for (Mail mail : mails) {
            VBox mailBox = createMailBox(mail);
            if(mail.getActif()){
                mailBox.getStyleClass().add("mailBox-actif");
            }else{
                mailBox.getStyleClass().add("mailBox");
            }
            mailList.getChildren().add(mailBox);
        }
    }

    @FXML
    private void delPJ(int index){
        Pjs.remove(index);
        PJsNames.remove(index);
        filesContainer.getChildren().remove(index);

        if (Pjs.isEmpty()) {
            attachmentsContainer.getChildren().clear();
            filesContainer.setVisible(false);
            filesContainer.setManaged(false);
        }
    }

    private void delPJ(){
        attachmentsContainer.getChildren().clear();
        filesContainer.getChildren().clear();
        filesContainer.setVisible(false);
        filesContainer.setManaged(false);
        Pjs.clear();
        PJsNames.clear();
    }

    @FXML
    private void respondMail(){
        newMsgDest.setText(senderMessage.getText());
        newMsgTitle.setText("Re : " + titreMessage.getText());
        newMsgMessage.setText("");
        newMsgVbox.setVisible(true);
        newMsgVbox.setManaged(true);
        openMsgVbox.setVisible(false);
        openMsgVbox.setManaged(false);
        //setTextarea(true,msgMessage.getText());
        if(!PJsNames.isEmpty()){
            for (String pj : PJsNames){
                addPJFromMail(pj);
            }
            filesContainer.setVisible(true);
            filesContainer.setManaged(true);
        }
    }

    @FXML
    private void transfertMail(){
        newMsgTitle.setText("Pwd : " + titreMessage.getText());
        newMsgDest.setText("");
        newMsgMessage.setText("");
        newMsgVbox.setVisible(true);
        newMsgVbox.setManaged(true);
        openMsgVbox.setVisible(false);
        openMsgVbox.setManaged(false);
        //setTextarea(true,msgMessage.getText());
        if(!PJsNames.isEmpty()){
            for (String pj : PJsNames){
                addPJFromMail(pj);
            }
            filesContainer.setVisible(true);
            filesContainer.setManaged(true);
        }
    }

    @FXML
    private void setTextarea(boolean resp, String obj){
        objRespContainer.setVisible(resp);
        objRespContainer.setManaged(resp);
        if(resp){
            objResp.setText("\""+obj+"\"");
        }
    }


    private void downloadFile(String sourceFilePath, String targetDirectoryPath) {
        try {
            Path sourcePath = Paths.get(sourceFilePath);
            Path targetDirectory = Paths.get(targetDirectoryPath);
            if (Files.exists(sourcePath) && Files.isDirectory(targetDirectory)) {
                String fileName = sourcePath.getFileName().toString();
                Path targetPath = Paths.get(targetDirectoryPath, fileName);
                Files.copy(sourcePath, targetPath);
                System.out.println("Le fichier a été déplacé avec succès vers le dossier cible.");
            } else {
                System.out.println("Le fichier source n'existe pas ou le chemin cible n'est pas un dossier valide.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setHtmlContent(boolean on, String content){
        msgContent.setManaged(on);
        msgContent.setVisible(on);
        msgContent.getEngine().loadContent((on) ? content : "");
    }

    public void setNotHtmlContent(boolean on, String content){
        msgMessage.setManaged(on);
        msgMessage.setVisible(on);
        msgMessage.setText((on) ? content : "");
    }

    public boolean isHtmlContent(String content) {
        return content.matches("(?s).*<\\s*html.*>.*");
    }

    public static String extractTextFromHtml(String htmlContent) {
        String text = htmlContent.replaceAll("\\<.*?\\>", "");
        int maxLength = 50;
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength) + "...";
        }
        return text;
    }

}