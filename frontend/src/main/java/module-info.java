module com.project.crud.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires static lombok;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    opens com.project.crud.frontend.model to javafx.base, javafx.fxml, com.fasterxml.jackson.databind;
    opens com.project.crud.frontend to javafx.fxml, com.google.gson;
    exports com.project.crud.frontend;
    exports com.project.crud.frontend.controllers;
    opens com.project.crud.frontend.controllers to com.google.gson, javafx.fxml;
}