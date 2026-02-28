package com.electricity.cms.service;

import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.User;
import com.electricity.cms.repository.ComplaintRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintService() {
        this.complaintRepository = new ComplaintRepository();
    }

    public void submitComplaint(String type,
                                String address,
                                String description,
                                User user) {

        Complaint complaint = new Complaint();

        complaint.setId(UUID.randomUUID());
        complaint.setType(type);
        complaint.setAddress(address);
        complaint.setDescription(description);
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setStatus("PENDING");
        complaint.setUser(user);   // IMPORTANT

        complaintRepository.save(complaint);
    }
}