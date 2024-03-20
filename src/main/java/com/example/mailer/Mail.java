package com.example.mailer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

public class Mail {
    private String title;
    private String sender;
    private String message;

    public Mail(String title, String sender, String message) {
        this.title = title;
        this.sender = sender;
        this.message = message;
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

}
