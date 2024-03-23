package com.example.mailer.mails;

import com.example.mailer.Context;
import com.example.mailer.crypto.ElGamal;
import com.example.mailer.utils.Constants;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mail {
    private String object;
    private String msg;
    private String date_envoie;
    private String[] pj;
    private boolean msg_vu;
    private String destinataire;
    private String expediteur;
    private Message message;

    // Constructeur Mail reçu avec pièces jointes
    public Mail(String object_, String sender, String msg_, String date, String[] attachements, boolean seen, String destinataire_, Message message_){
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
        this.pj = new String[0];
        this.msg_vu = seen;
        this.destinataire = destinataire_;
        this.expediteur = sender;
        this.message = message_;
    }

    // Contructeur mail à envoyer avec pièces jointes
    public Mail(String expediteur_, String destinataire_, String[] attachements){
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
        this.pj = new String[0];
        this.destinataire = destinaire_;
        this.expediteur = expediteur_;
        this.msg_vu = false;
    }

    /*
     * Envoie un mail sans pièces jointes
     */
    public static void sendMessage(String user, String password, String destination, String subject, String text) {
        Properties properties = new Properties();

        properties.put("mail.smtp.host", Constants.MAIL_HOST);
        properties.put("mail.smtp.port", Constants.SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user,password);
            }
        });
        System.out.println("session.getProviders():" + session.getProviders()[0].getType());
        try {
            MimeMessage message = new MimeMessage(session);
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
        Session session = Context.EMAIL_SESSION;
        System.out.println("session.getProviders():" + session.getProviders()[0].getType());
        try {
            MimeMessage message = new MimeMessage(session);
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
    public static Mail[] get_list_mail(String userName, String password) {
        Properties properties = new Properties();
        Mail[] list_mail = new Mail[0];


        // server setting (it can be pop3 too
        properties.put("mail.imap.host", Constants.MAIL_HOST);
        properties.put("mail.imap.port", Constants.IMAP_PORT);
        properties.setProperty("mail.imap.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port", Constants.IMAP_PORT);

        Session session = Session.getDefaultInstance(properties);

        try {
            Store store = session.getStore("imap");

            store.connect(userName, password);

            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            // fetches new messages from server
            Message[] arrayMessages = folderInbox.getMessages();

            for (int i = 0; i < arrayMessages.length; i++) {
                Mail mail;
                Message message = arrayMessages[i];
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
                String subject = message.getSubject();
                String sentDate = message.getSentDate().toString();
                String contentType = message.getContentType();
                String messageContent = "";
                boolean message_seen = message.getFlags().contains(Flags.Flag.SEEN);
                // store attachment file name, separated by comma
                String attachFiles = "";

                if (contentType.contains("multipart")) {
                    // content may contain attachments
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            // this part is attachment
                            String fileName = part.getFileName();
                            attachFiles += fileName + "/";
                        } else {
                            // this part may be the message content
                            messageContent = part.getContent().toString();
                        }
                    }

                    if (attachFiles.length() > 1) {
                        attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                    }
                } else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content = message.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                }

                if(attachFiles == ""){
                    mail = new Mail(subject, from, messageContent, sentDate, message_seen, userName, message);
                }
                else{
                    String[] attachements = attachFiles.split("/");
                    mail = new Mail(subject, from, messageContent, sentDate, attachements, message_seen, userName, message);
                }

                list_mail[i] = mail;
            }

            // disconnect
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for imap.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return list_mail;
    }

    /*
     * Fonction pour télécharger les pièces jointes CHIFFRÉES du mail, avec le déchiffrement
     */
    public void downloadEmailAttachments(Mail m){
        Message msg = m.get_message();

        try {
            String contentType;
            contentType = msg.getContentType();

            if(contentType.contains("multipart")) {
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();
                        part.saveFile(Constants.ENC_ATTACHMENTS_PATH + File.separator + "todec_" + fileName);
                    }
                }}

            // TODO: parcourir /tmp/ pour déchiffrer les fichiers todec_... et les supprimer
            // est ce que c'est possible de récuperer le contenu directement pour pas avoir à écrire puis déchiffrer ?
            // ElGamal.decryptAttachment(...);

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
    public String get_object(){
        return object;
    }

    /*
     * Set l'object du mail
     */
    public void set_object(String object_){
        this.object = object_;
    }

    /*
     * Retourne le message du mail
     */
    public String get_contenu(){
        return msg;
    }

    /*
     * Set le message du mail
     */
    public void set_contenu(String msg_){
        this.msg = msg_;
    }

    /*
     * Retourne la date d'envoie du mail
     */
    public String get_date(){
        return date_envoie;
    }

    /*
     * Set la date d'envoie du mail
     */
    public void set_date(String date){
        this.date_envoie = date;
    }

    /*
     * Retourne la liste des pièces jointes
     */
    public String[] get_attachements(){
        return pj;
    }

    /*
     * Set les pièces jointes
     */
    public void set_attachements(String[] attachements){
        this.pj = attachements;
    }

    /*
     * Retourne si le message a été vu
     */
    public boolean get_seen(){
        return msg_vu;
    }

    /*
     * Set si le message a été vu ou non
     */
    public void set_seen(boolean seen){
        this.msg_vu = seen;
    }

    /*
     * Retourne le destinataire du mail
     */
    public String get_destinataire(){
        return destinataire;
    }

    /*
     * Set le destinataire du mail
     */
    public void set_destinataire(String destinataire_){
        this.destinataire = destinataire_;
    }

    /*
     * Retourne l'expéditeur du mail
     */
    public String get_expediteur(){
        return expediteur;
    }

    /*
     * Set l'expéditeur du mail
     */
    public void set_expediteur(String expediteur_){
        this.expediteur = expediteur_;
    }

    /*
     * Retourne le message à l'état originel
     */
    public Message get_message(){
        return message;
    }

    /*
     * Set le message à l'état originel du mail
     */
    public void set_message(Message message_){
        this.message = message_;
    }
}