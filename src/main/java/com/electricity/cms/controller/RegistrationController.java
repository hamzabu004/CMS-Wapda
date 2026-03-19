package com.electricity.cms.controller;

import java.time.LocalDate;
import java.util.UUID;

import com.electricity.cms.model.ConnType;
import com.electricity.cms.model.Consumer;
import com.electricity.cms.model.Person;
import com.electricity.cms.model.Region;
import com.electricity.cms.repository.RegionRepository;
import com.electricity.cms.service.ConsumerService;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {

    @FXML private TextField fullNameField;
    @FXML private TextField cnicField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField consumerReferenceField;
    @FXML private TextField meterNumberField;
    @FXML private ComboBox<ConnType> connectionTypeCombo;
    @FXML private TextField meterAddressField;
    @FXML private TextField regionIdField;
    @FXML private Label statusLabel;

    private final ConsumerService consumerService = new ConsumerService();
    private final RegionRepository regionRepository = new RegionRepository();

    @FXML
    public void initialize() {
        connectionTypeCombo.getItems().setAll(ConnType.values());
        connectionTypeCombo.setValue(ConnType.RESIDENT);
    }

    @FXML
    private void registerConsumer() {
        try {
            Person person = new Person();
            person.setFullName(fullNameField.getText());
            person.setCnic(cnicField.getText());
            person.setPhoneNumber(phoneField.getText());

            Consumer consumer = new Consumer();
            consumer.setConsumerReference(consumerReferenceField.getText());
            consumer.setMeterNumber(meterNumberField.getText());
            consumer.setConnectionType(connectionTypeCombo.getValue());
            consumer.setInstallationDate(LocalDate.now());
            consumer.setMeterAddress(meterAddressField.getText());

            Region region = regionRepository.findById(UUID.fromString(regionIdField.getText()))
                .orElseThrow(() -> new IllegalArgumentException("Region not found."));
            consumer.setRegion(region);

            consumerService.registerConsumer(person, consumer, emailField.getText(), passwordField.getText());
            statusLabel.setText("Consumer registered successfully.");
        } catch (Exception ex) {
            statusLabel.setText("Registration failed: " + ex.getMessage());
        }
    }
}
