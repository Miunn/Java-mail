module com.example.mailer {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.mailer to javafx.fxml;
    exports com.example.mailer;
}