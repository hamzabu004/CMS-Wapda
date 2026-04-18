package com.electricity.cms.controller;

import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.ComplaintMessage;
import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.service.ComplaintMessageService;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ComplaintThreadController {

    @FXML private VBox messagesContainer;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private ScrollPane scrollPane;

    private Complaint complaint;
    private UserContext currentUser;

    private final ComplaintMessageService messageService = new ComplaintMessageService();

    // Called from previous screen
    public void initData(Complaint complaint, UserContext userContext) {
        this.complaint = complaint;
        this.currentUser = userContext;

        loadMessages();
        updateInputState();
    }

    private void loadMessages() {
        if (complaint == null) return;

        messagesContainer.getChildren().clear();

        List<ComplaintMessage> messages =
                messageService.getMessages(complaint.getId());

        for (ComplaintMessage msg : messages) {
            messagesContainer.getChildren().add(createMessageBubble(msg));
        }

        scrollPane.setVvalue(1.0); // scroll to bottom
    }

    private HBox createMessageBubble(ComplaintMessage msg) {

        boolean isMine = msg.getSenderId().equals(currentUser.userId());

        VBox bubble = new VBox();
        bubble.setSpacing(3);

        Label name = new Label(msg.getSenderName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label role = new Label(msg.getSenderRole().toString());
        role.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        Label text = new Label(msg.getMessageText());
        text.setWrapText(true);

        Label time = new Label(
                msg.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))
        );
        time.setStyle("-fx-font-size: 9px; -fx-opacity: 0.6;");

        bubble.getChildren().addAll(name, role, text, time);

        bubble.setStyle(isMine
                ? "-fx-background-color: #3a5a3c; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 8;"
                : "-fx-background-color: #f5f0e8; -fx-padding: 8; -fx-background-radius: 8;"
        );

        HBox container = new HBox(bubble);
        container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        return container;
    }

    @FXML
    private void handleSend() {

        String text = messageField.getText();

        if (text == null || text.trim().isEmpty()) return;

        // ✅ CUSTOMER BLOCK LOGIC (using enum instead of string)
        if (currentUser.role() == UserRole.CUSTOMER
                && complaint.isCustomerBlocked()) {

            showAlert("You must wait for a reply before sending another message.");
            return;
        }

        // ✅ CORRECT CALL
        messageService.sendMessage(complaint.getId(), currentUser, text);

        messageField.clear();

        loadMessages();
        updateInputState();
    }

    private void updateInputState() {

        if (currentUser.role() == UserRole.CUSTOMER) {
            messageField.setDisable(complaint.isCustomerBlocked());
            sendButton.setDisable(complaint.isCustomerBlocked());
        } else {
            messageField.setDisable(false);
            sendButton.setDisable(false);
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}