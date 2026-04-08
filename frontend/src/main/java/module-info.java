module com.project.crud.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires static lombok;
    requires java.desktop;
    exports com.project.crud.frontend;
    exports com.project.crud.frontend.controllers;
    opens com.project.crud.frontend to javafx.fxml;
    opens com.project.crud.frontend.controllers to javafx.fxml;
    opens com.project.crud.frontend.model to javafx.base, com.fasterxml.jackson.databind;
    opens com.project.crud.frontend.auth to com.fasterxml.jackson.databind;
}