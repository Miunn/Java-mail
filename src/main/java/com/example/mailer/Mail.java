package com.example.mailer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

import java.io.File;

public class Mail {
    private String title;
    private String sender;
    private String message;
    private File pj;

    public Mail(String title, String sender, String message) {
        this.title = title;
        this.sender = sender;
        this.message = message;
        this.pj = null;
    }

    public Mail(String title, String sender, String message, File pj) {
        this.title = title;
        this.sender = sender;
        this.message = message;
        this.pj = pj;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public StringBinding titleProperty() {
        return Bindings.createStringBinding(this::getTitle);
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public StringBinding senderProperty() {
        return Bindings.createStringBinding(this::getTitle);
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public StringBinding messageProperty() {
        return Bindings.createStringBinding(this::getTitle);
    }

    public File getPj() {
        return pj;
    }

    public void setPj(File pj) {
        this.pj = pj;
    }
}
