module com.project.crud.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires static lombok;
    requires java.net.http;

    opens com.project.crud.frontend to javafx.fxml, com.google.gson;
    exports com.project.crud.frontend;
    exports com.project.crud.frontend.controllers;
    opens com.project.crud.frontend.controllers to com.google.gson, javafx.fxml;
}