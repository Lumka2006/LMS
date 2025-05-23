module com.lms {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens com.lms to javafx.fxml;
    opens com.lms.controller to javafx.fxml;
    opens com.lms.model to javafx.base;
    opens com.lms.service to javafx.base;
    opens com.lms.util to javafx.base;
    
    exports com.lms;
    exports com.lms.controller;
    exports com.lms.model;
    exports com.lms.service;
    exports com.lms.util;
} 