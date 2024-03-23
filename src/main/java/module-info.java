module com.example.mailer {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.logging;
    requires activation;
    requires jpbc.api;
    requires jpbc.plaf;
    requires java.mail;
    requires bcprov.jdk16;
    requires java.net.http;
    requires javax.json;

    opens com.example.mailer to javafx.fxml;
    exports com.example.mailer;
}