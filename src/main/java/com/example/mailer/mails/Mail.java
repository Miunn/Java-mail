package com.example.mailer.mails;

import com.example.mailer.Context;
import com.example.mailer.crypto.ElGamal;
import com.example.mailer.utils.Constants;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mail {
    private String object;
    private String msg;
    private String date_envoie;
    private List<String> pj;  // liste des noms des pièces jointes
    private boolean msg_vu;
    private String destinataire;
    private String expediteur;
    private Message message;

    private Boolean actif=false;

    // Constructeur Mail reçu avec pièces jointes
    public Mail(String object_, String sender, String msg_, String date, List<String> attachements, boolean seen, String destinataire_, Message message_){
        this.object = object_;
        this.msg = msg_;
        this.date_envoie = date;
        this.pj = attachements;
        this.msg_vu = seen;
        this.destinataire = destinataire_;
        this.expediteur = sender;
        this.message = message_;
    }

    // Constructeur Mail reçu sans pièces jointes
    public Mail(String object_, String sender, String msg_, String date, boolean seen, String destinataire_, Message message_){
        this.object = object_;
        this.msg = msg_;
        this.date_envoie = date;
        this.pj = new ArrayList<>(0);
        this.msg_vu = seen;
        this.destinataire = destinataire_;
        this.expediteur = sender;
        this.message = message_;
    }

    // Contructeur mail à envoyer avec pièces jointes
    public Mail(String expediteur_, String destinataire_, List<String> attachements){
        this.object = "";
        this.msg = "";
        this.date_envoie = "";
        this.pj = attachements;
        this.destinataire = destinataire_;
        this.expediteur = expediteur_;
        this.msg_vu = false;
    }

    // Constructeur mail à envoyer sans pièces jointes
    public Mail(String expediteur_, String destinaire_){
        this.object = "";
        this.msg = "";
        this.date_envoie = "";
        this.pj = new ArrayList<>(0);
        this.destinataire = destinaire_;
        this.expediteur = expediteur_;
        this.msg_vu = false;
    }

    /*
     * Envoie un mail sans pièces jointes
     */
    public static void sendMessage(String user, String destination, String subject, String text) {

        if(!Context.isConnected()) {
            System.out.println("PAS CONNECTE");
            return;
        }
        System.out.println("session.getProviders():" +  Context.EMAIL_SESSION.getProviders()[0].getType());
        try {
            MimeMessage message = new MimeMessage(Context.EMAIL_SESSION);
            message.setFrom(user);
            message.setSubject(subject);
            message.setText(text);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destination));

            System.out.println("Message en cours d'envoie");

            Transport.send(message);
            System.out.println("Message envoyé");

        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    // Envoie de message avec pièces jointes
    public static void sendMessageWithAttachement(String user, String destination,
                                                  String attachement_path,String fileName, String subject, String text) {
        if(!Context.isConnected()) {
            System.out.println("PAS CONNECTE");
            return;
        }
        System.out.println("session.getProviders():" + Context.EMAIL_SESSION.getProviders()[0].getType());
        try {
            MimeMessage message = new MimeMessage(Context.EMAIL_SESSION);
            message.setFrom(user);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject(subject);

            Multipart myemailcontent = new MimeMultipart();
            MimeBodyPart bodypart = new MimeBodyPart();
            bodypart.setText(text);

            MimeBodyPart attachementfile = new MimeBodyPart();
            DataSource source = ElGamal.encryptAttachment(attachement_path, fileName);
            attachementfile.setDataHandler(new DataHandler(source));
            attachementfile.attachFile(attachement_path);
            myemailcontent.addBodyPart(bodypart);
            myemailcontent.addBodyPart(attachementfile);
            message.setContent(myemailcontent);
            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(Mail.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Permet de récupérer tous les mails de la boîte de réception de user
     */
    public static List<Mail> getMailList(String userName) {
        try {
            // opens the inbox folder
            Folder folderInbox = Context.STORE.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            // fetches new messages from server
            Message[] am = folderInbox.getMessages();
            int len = am.length;
            List<Message> arrayMessages = List.of(am).subList(len-Constants.DISPLAY_NB, len);
            List<Mail> list_mail = new ArrayList<>();

            for (int i = Constants.DISPLAY_NB-1; i >= 0 ; i--) {
                Mail mail;
                Message message = arrayMessages.get(i);
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
                String subject = message.getSubject();
                String sentDate = message.getSentDate().toString();
                String contentType = message.getContentType();
                String messageContent = "";
                boolean message_seen = message.getFlags().contains(Flags.Flag.SEEN);
                // store attachment file name, separated by comma
                StringBuilder attachFiles = new StringBuilder();

                if (contentType.contains("multipart")) {
                    // content may contain attachments
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            // this part is attachment
                            String fileName = part.getFileName();
                            attachFiles.append(fileName).append("/");
                        } else {
                            // this part may be the message content
                            messageContent = part.getContent().toString();
                        }
                    }

                    if (attachFiles.length() > 1) {
                        attachFiles = new StringBuilder(attachFiles.substring(0, attachFiles.length() - 2));
                    }
                } else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content = message.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                }

                if(attachFiles.toString().equals("")){
                    mail = new Mail(subject, from, messageContent, sentDate, message_seen, userName, message);
                }
                else{
                    List<String> attachements = List.of(attachFiles.toString().split("/"));
                    mail = new Mail(subject, from, messageContent, sentDate, attachements, message_seen, userName, message);
                }

                list_mail.add(mail);
            }

            // disconnect
            folderInbox.close(false);

            return list_mail;
        } catch (IOException | MessagingException ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    /*
     * Fonction pour télécharger les pièces jointes CHIFFRÉES du mail, avec le déchiffrement
     */
    public void downloadEmailAttachments(String destinationPath){
        Message msg = this.getMessage();

        try {
            String contentType;
            contentType = msg.getContentType();
            List<List<String>> pathList = new ArrayList<>(0);
            if(contentType.contains("multipart")) {
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();
                        part.saveFile(Constants.ENC_ATTACHMENTS_PATH + File.separator + "_" + fileName);
                        pathList.add(
                                List.of(Constants.ENC_ATTACHMENTS_PATH+File.separator, "_" + fileName)
                        );
                    }
                }}

            // Parcours de /tmp/ pour déchiffrer les fichiers todec_...
            for (List<String> path : pathList) {
                ElGamal.decryptAttachment(path.get(0), path.get(1));
            }

            // retourner la liste des path des fichiers déchiffrés

        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for imap.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Getter & Setter
    /*
     * Retourne l'object du mail
     */
    public String getObject(){
        return object;
    }

    /*
     * Set l'object du mail
     */
    public void setObject(String object_){
        this.object = object_;
    }

    /*
     * Retourne le message du mail
     */
    public String getMessageContent(){
        return msg;
    }

    /*
     * Set le message du mail
     */
    public void setMessageContent(String msg_){
        this.msg = msg_;
    }

    /*
     * Retourne la date d'envoie du mail
     */
    public String getDate(){
        return date_envoie;
    }

    /*
     * Set la date d'envoie du mail
     */
    public void setDate(String date){
        this.date_envoie = date;
    }

    /*
     * Retourne la liste des pièces jointes
     */
    public List<String> getAttachements(){
        return pj;
    }

    /*
     * Set les pièces jointes
     */
    public void setAttachements(List<String> attachements){
        this.pj = attachements;
    }

    /*
     * Retourne si le message a été vu
     */
    public boolean getSeen(){
        return msg_vu;
    }

    /*
     * Set si le message a été vu ou non
     */
    public void setSeen(boolean seen){
        this.msg_vu = seen;
    }

    /*
     * Retourne le destinataire du mail
     */
    public String getReceiver(){
        return destinataire;
    }

    /*
     * Set le destinataire du mail
     */
    public void setReceiver(String destinataire_){
        this.destinataire = destinataire_;
    }

    /*
     * Retourne l'expéditeur du mail
     */
    public String getSender(){
        return expediteur;
    }

    /*
     * Set l'expéditeur du mail
     */
    public void setSender(String expediteur_){
        this.expediteur = expediteur_;
    }

    /*
     * Retourne le message à l'état originel
     */
    public Message getMessage(){
        return message;
    }

    /*
     * Set le message à l'état originel du mail
     */
    public void setMessage(Message message_){
        this.message = message_;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}