package com.electricity.cms.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Manages a single shared EntityManagerFactory for the application lifetime.
 * DB credentials are injected from .env via EnvLoader.
 */
public class DatabaseUtil {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());
    private static EntityManagerFactory emf;

    private DatabaseUtil() {}

    /**
     * Initialises the EntityManagerFactory once.
     * Call from MainApp.init() before any DB access.
     *
     * @throws RuntimeException if required DB env vars are missing or the connection fails
     */
    public static synchronized void initialize() {
        if (emf != null && emf.isOpen()) {
            return;
        }
        try {
            Map<String, String> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url",      resolveJdbcUrl());
            props.put("jakarta.persistence.jdbc.user",     EnvLoader.get("DB_USERNAME"));
            props.put("jakarta.persistence.jdbc.password", EnvLoader.get("DB_PASSWORD"));
            props.put("jakarta.persistence.jdbc.driver",   "org.postgresql.Driver");

            String ddlMode = resolveDdlMode();
            props.put("hibernate.hbm2ddl.auto", ddlMode);
            props.put("hibernate.show_sql",
                      EnvLoader.getOrDefault("DB_SHOW_SQL", "false"));
            props.put("hibernate.format_sql", "true");
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

            emf = Persistence.createEntityManagerFactory("electricity-cms-pu", props);
            LOGGER.info("[DatabaseUtil] EntityManagerFactory initialised successfully.");
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "[DatabaseUtil] Failed to initialise database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the shared EntityManagerFactory.
     *
     * @throws IllegalStateException if initialize() has not been called yet
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            throw new IllegalStateException(
                    "[DatabaseUtil] EntityManagerFactory is not initialised. " +
                    "Call DatabaseUtil.initialize() at application startup.");
        }
        return emf;
    }

    /**
     * Closes the EntityManagerFactory. Call from MainApp.stop().
     */
    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            LOGGER.info("[DatabaseUtil] EntityManagerFactory closed.");
        }
    }

    private static String resolveDdlMode() {
        String allowSchemaUpdate = EnvLoader.getOrDefault("DB_ALLOW_SCHEMA_UPDATE", "false");
        if (!"true".equalsIgnoreCase(allowSchemaUpdate)) {
            return "none";
        }

        String configured = EnvLoader.getOrDefault("DB_DDL_AUTO", "none").trim().toLowerCase();
        if (configured.isEmpty()) {
            return "none";
        }

        return switch (configured) {
            case "none", "validate", "update", "create", "create-drop" -> configured;
            default -> {
                LOGGER.warning("[DatabaseUtil] Unsupported DB_DDL_AUTO='" + configured + "'. Falling back to 'none'.");
                yield "none";
            }
        };
    }

    private static String resolveJdbcUrl() {
        String url = EnvLoader.get("DB_URL");
        String disablePreparedStatements = EnvLoader.getOrDefault("DB_DISABLE_SERVER_PREPARED", "true");
        if (!"true".equalsIgnoreCase(disablePreparedStatements)) {
            return url;
        }

        if (url.contains("preferQueryMode=")) {
            return url;
        }

        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "preferQueryMode=simple";
    }
}

