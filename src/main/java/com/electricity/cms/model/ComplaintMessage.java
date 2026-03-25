package com.electricity.cms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaint_messages")
public class ComplaintMessage {

    @Id
    private UUID id;

    private UUID complaintId;
    private UUID senderId;

    private String senderName;

    @Enumerated(EnumType.STRING)
    private UserRole senderRole;

    private String messageText;

    private LocalDateTime createdAt;

    // ===== GETTERS & SETTERS =====

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getComplaintId() { return complaintId; }
    public void setComplaintId(UUID complaintId) { this.complaintId = complaintId; }

    public UUID getSenderId() { return senderId; }
    public void setSenderId(UUID senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public UserRole getSenderRole() { return senderRole; }
    public void setSenderRole(UserRole senderRole) { this.senderRole = senderRole; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}