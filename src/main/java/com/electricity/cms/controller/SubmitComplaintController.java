package com.electricity.cms.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import com.electricity.cms.model.User;
import com.electricity.cms.service.ComplaintService;
@FXML
private void handleSubmitComplaint() {

    String type = complaintTypeCombo.getValue();
    String address = addressField.getText();
    String description = descriptionArea.getText();

    User currentUser = LoginController.getLoggedInUser();

    ComplaintService service = new ComplaintService();
    service.submitComplaint(type, address, description, currentUser);

    statusLabel.setText("Complaint submitted successfully!");
    statusLabel.setVisible(true);
}