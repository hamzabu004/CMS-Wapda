package com.electricity.cms.service;

import com.electricity.cms.model.*;
import com.electricity.cms.repository.ComplaintMessageRepository;
import com.electricity.cms.repository.ComplaintRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ComplaintMessageService {

    private final ComplaintMessageRepository messageRepo = new ComplaintMessageRepository();
    private final ComplaintRepository complaintRepo = new ComplaintRepository();

    public void sendMessage(UUID complaintId, String text, User user) {

        Complaint complaint = complaintRepo.findById(complaintId).orElse(null);
        if (complaint == null) return;

        // 🚫 BLOCK LOGIC
        if (user.getRole() == UserRole.CUSTOMER && complaint.isCustomerBlocked()) {
            return;
        }

        ComplaintMessage msg = new ComplaintMessage();
        msg.setId(UUID.randomUUID());
        msg.setComplaintId(complaintId);
        msg.setSenderId(user.getId());
        msg.setSenderName(user.getUsername());
        msg.setSenderRole(user.getRole());
        msg.setMessageText(text);
        msg.setCreatedAt(LocalDateTime.now());

        messageRepo.save(msg);

        // ✅ Update complaint state
        complaint.setLastSenderRole(user.getRole());

        if (user.getRole() == UserRole.CUSTOMER) {
            complaint.setCustomerBlocked(true);
        } else {
            complaint.setCustomerBlocked(false);
        }

        complaintRepo.update(complaint);
    }

    public List<ComplaintMessage> getMessages(UUID complaintId) {
        return messageRepo.findByComplaintId(complaintId);
    }
}