package com.electricity.cms.service;

import com.electricity.cms.model.*;
import com.electricity.cms.dto.UserContext;
import com.electricity.cms.repository.ComplaintMessageRepository;
import com.electricity.cms.repository.ComplaintRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ComplaintMessageService {

    private final ComplaintMessageRepository messageRepo = new ComplaintMessageRepository();
    private final ComplaintRepository complaintRepo = new ComplaintRepository();

    //  FIXED METHOD SIGNATURE
    public void sendMessage(UUID complaintId, UserContext user, String text) {

        Complaint complaint = complaintRepo.findById(complaintId).orElse(null);
        if (complaint == null) return;

        //  BLOCK LOGIC
        if (user.role() == UserRole.CUSTOMER && complaint.isCustomerBlocked()) {
            return;
        }

        ComplaintMessage msg = new ComplaintMessage();
        msg.setId(UUID.randomUUID());
        msg.setComplaintId(complaintId);

        //  USING UserContext
        msg.setSenderId(user.userId());
        msg.setSenderName(user.userId().toString()); // if error, replace with user.userId().toString()
        msg.setSenderRole(user.role());

        msg.setMessageText(text);
        msg.setCreatedAt(LocalDateTime.now());

        messageRepo.save(msg);

        //  Update complaint state
        complaint.setLastSenderRole(user.role());

        if (user.role() == UserRole.CUSTOMER) {
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