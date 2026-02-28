module com.electricity.cms {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // Persistence
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // PostgreSQL (accessed via Hibernate, but needed for module path)
    requires org.postgresql.jdbc;

    // .env loader
    requires io.github.cdimascio.dotenv.java;

    // Jakarta Mail
    requires jakarta.mail;

    // Logging
    requires java.logging;

    // Open model package to Hibernate for reflection
    opens com.electricity.cms.model to org.hibernate.orm.core, javafx.base;

    // Open controller and app packages to JavaFX FXML
    opens com.electricity.cms.controller to javafx.fxml;
    opens com.electricity.cms.app        to javafx.fxml;

    // Exports
    exports com.electricity.cms.app;
    exports com.electricity.cms.controller;
    exports com.electricity.cms.model;
    exports com.electricity.cms.repository;
    exports com.electricity.cms.util;
}