package com.electricity.cms.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton SMTP email sender.
 *
 * A single jakarta.mail.Transport connection is opened once at application
 * startup via initialize() and reused for every sendMail() call.
 * The connection is closed cleanly by shutdown() when the app exits.
 *
 * If the connection drops between sends, sendMail() detects it and
 * attempts a single reconnect before failing with a clear log message.
 */
public class EmailSender {

    private static final Logger LOGGER = Logger.getLogger(EmailSender.class.getName());

    private static EmailSender instance;

    private Session session;
    private Transport transport;

    private String host;
    private int    port;
    private String username;
    private String password;
    private String from;

    private EmailSender() {}

    /** Returns the singleton instance. */
    public static synchronized EmailSender getInstance() {
        if (instance == null) {
            instance = new EmailSender();
        }
        return instance;
    }

    /**
     * Reads SMTP credentials from .env and opens the persistent Transport connection.
     * Call once from MainApp.init().
     *
     * Required .env keys:
     *   SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM
     *
     * @throws RuntimeException if credentials are missing or the connection fails
     */
    public synchronized void initialize() {
        host     = EnvLoader.get("SMTP_HOST");
        port     = Integer.parseInt(EnvLoader.get("SMTP_PORT"));
        username = EnvLoader.get("SMTP_USERNAME");
        password = EnvLoader.get("SMTP_PASSWORD");
        from     = EnvLoader.get("SMTP_FROM");

        Properties props = new Properties();
        props.put("mail.smtp.host",            host);
        props.put("mail.smtp.port",            String.valueOf(port));
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        connect();
        LOGGER.info("[EmailSender] SMTP Transport connected to " + host + ":" + port);
    }

    /**
     * Sends an email, reusing the already-open Transport connection.
     * Reconnects once if the connection has dropped.
     *
     * @param to      recipient address
     * @param subject email subject
     * @param body    plain-text body
     */
    public void sendMail(String to, String subject, String body) {
        if (transport == null) {
            throw new IllegalStateException(
                    "[EmailSender] Not initialised. Call EmailSender.getInstance().initialize() at startup.");
        }

        try {
            ensureConnected();

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            transport.sendMessage(message, message.getAllRecipients());
            LOGGER.info("[EmailSender] Mail sent to " + to + " | Subject: " + subject);

        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE,
                    "[EmailSender] Failed to send mail to " + to + ": " + e.getMessage(), e);
        }
    }

    /**
     * Checks whether the Transport is still connected; reconnects once if not.
     */
    private void ensureConnected() throws MessagingException {
        if (!transport.isConnected()) {
            LOGGER.warning("[EmailSender] Transport disconnected — attempting reconnect...");
            connect();
            if (!transport.isConnected()) {
                throw new MessagingException(
                        "[EmailSender] Reconnect to SMTP server failed.");
            }
            LOGGER.info("[EmailSender] Reconnected to SMTP server.");
        }
    }

    /** Opens the Transport connection. */
    private void connect() {
        try {
            transport = session.getTransport("smtp");
            transport.connect(host, port, username, password);
        } catch (MessagingException e) {
            throw new RuntimeException(
                    "[EmailSender] Could not connect to SMTP server " + host + ":" + port +
                    " — " + e.getMessage(), e);
        }
    }

    /**
     * Closes the Transport connection. Call from MainApp.stop().
     */
    public synchronized void shutdown() {
        if (transport != null && transport.isConnected()) {
            try {
                transport.close();
                LOGGER.info("[EmailSender] SMTP Transport closed.");
            } catch (MessagingException e) {
                LOGGER.log(Level.WARNING, "[EmailSender] Error closing Transport: " + e.getMessage(), e);
            }
        }
    }
}

