package com.electricity.cms.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.electricity.cms.dto.DashboardStats;
import com.electricity.cms.dto.DateRange;
import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.ComplaintCategory;
import com.electricity.cms.model.ComplaintStatus;
import com.electricity.cms.model.ComplaintStatusHistory;
import com.electricity.cms.model.Consumer;
import com.electricity.cms.model.User;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.repository.ComplaintRepository;
import com.electricity.cms.repository.ComplaintStatusHistoryRepository;
import com.electricity.cms.repository.ConsumerRepository;
import com.electricity.cms.repository.UserRepository;

public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final ConsumerRepository consumerRepository;
    private final UserRepository userRepository;

    public ComplaintService() {
        this(
            new ComplaintRepository(),
            new ComplaintStatusHistoryRepository(),
            new ConsumerRepository(),
            new UserRepository()
        );
    }

    public ComplaintService(
        ComplaintRepository complaintRepository,
        ComplaintStatusHistoryRepository historyRepository,
        ConsumerRepository consumerRepository,
        UserRepository userRepository
    ) {
        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.consumerRepository = consumerRepository;
        this.userRepository = userRepository;
    }

    public Complaint submitComplaint(UUID consumerId, ComplaintCategory category) {
        Consumer consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new IllegalArgumentException("Consumer not found."));

        Complaint complaint = new Complaint();
        complaint.setConsumer(consumer);
        complaint.setCategory(category);
        complaint.setStatus(ComplaintStatus.PENDING);
        return complaintRepository.save(complaint);
    }

    public List<Complaint> getComplaintsForConsumer(UUID consumerId) {
        return complaintRepository.findByConsumerId(consumerId);
    }

    public List<Complaint> getUnassignedQueue() {
        return complaintRepository.findUnassigned();
    }

    public void assignToRepresentative(UUID complaintId, UUID repId) {
        Complaint complaint = getComplaintOrThrow(complaintId);
        User representative = getUserOrThrow(repId);

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setChangedBy(representative);
        history.setStatus(ComplaintStatus.IN_PROGRESS);
        history.setNote("ASSIGNED_TO_REP:" + representative.getId());
        historyRepository.save(history);

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintRepository.update(complaint);
    }

    public void escalate(UUID complaintId, UUID escalatedByUserId) {
        Complaint complaint = getComplaintOrThrow(complaintId);
        User caller = getUserOrThrow(escalatedByUserId);

        if (caller.getRole() == UserRole.REPRESENTATIVE) {
            UUID regionId = complaint.getConsumer().getRegion().getId();
            User technician = userRepository.findTechniciansByRegion(regionId).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No technician available in complaint region."));
            createEscalationHistory(complaint, caller, "ESCALATED_TO_TECHNICIAN:" + technician.getId());
            return;
        }

        if (caller.getRole() == UserRole.TECHNICIAN) {
            if (caller.getRegion() == null) {
                throw new IllegalStateException("Technician has no region configured.");
            }
            Optional<User> manager = userRepository.findByRole(UserRole.MANAGER).stream()
                .filter(u -> u.getRegion() != null && caller.getRegion().getId().equals(u.getRegion().getId()))
                .findFirst();
            User managerUser = manager.orElseThrow(() -> new IllegalStateException("No manager in technician region."));
            createEscalationHistory(complaint, caller, "ESCALATED_TO_MANAGER:" + managerUser.getId());
            return;
        }

        throw new IllegalArgumentException("Escalation is only allowed for representative or technician.");
    }

    public List<Complaint> getComplaintsByRegion(UUID regionId, DateRange range) {
        return complaintRepository.findByRegionId(regionId).stream()
            .filter(c -> isWithinDateRange(c, range))
            .toList();
    }

    public List<Complaint> getFilteredComplaints(UUID userId, UserRole role, String filter, DateRange range) {
        String normalized = filter == null ? "ALL" : filter.trim().toUpperCase(Locale.ROOT);
        List<Complaint> base = complaintsVisibleToRole(userId, role);

        return base.stream()
            .filter(c -> isWithinDateRange(c, range))
            .filter(c -> matchesFilter(c, normalized))
            .sorted(Comparator.comparing(Complaint::getLastUpdated).reversed())
            .collect(Collectors.toList());
    }

    public DashboardStats getDashboardStats(UUID userId, UserRole role, DateRange range) {
        List<Complaint> complaints = getFilteredComplaints(userId, role, "ALL", range);
        DashboardStats stats = new DashboardStats();
        stats.setTotalComplaints(complaints.size());
        stats.setResolved(complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count());
        stats.setUnresolved(complaints.stream().filter(c -> c.getStatus() != ComplaintStatus.RESOLVED).count());
        stats.setEscalated(complaints.stream().filter(this::isEscalatedComplaint).count());

        if (role == UserRole.REPRESENTATIVE || role == UserRole.MANAGER) {
            User user = getUserOrThrow(userId);
            UUID regionId = user.getRegion() != null ? user.getRegion().getId() : null;
            long totalTech = regionId == null ? 0 : userRepository.findTechniciansByRegion(regionId).size();
            long assigned = complaints.stream().filter(this::isAssignedToTechnician).count();
            stats.setAssignedTechnicians(Math.min(assigned, totalTech));
            stats.setFreeTechnicians(Math.max(totalTech - assigned, 0));
        }

        if (role == UserRole.REPRESENTATIVE) {
            stats.setPendingUnassigned(getUnassignedQueue().size());
        }

        return stats;
    }

    private void createEscalationHistory(Complaint complaint, User caller, String note) {
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setChangedBy(caller);
        history.setStatus(ComplaintStatus.IN_PROGRESS);
        history.setNote(note);
        historyRepository.save(history);

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintRepository.update(complaint);
    }

    private boolean isWithinDateRange(Complaint complaint, DateRange range) {
        if (range == null) {
            return true;
        }
        return (complaint.getCreatedAt().isEqual(range.from()) || complaint.getCreatedAt().isAfter(range.from()))
            && (complaint.getCreatedAt().isEqual(range.to()) || complaint.getCreatedAt().isBefore(range.to()));
    }

    private boolean matchesFilter(Complaint complaint, String filter) {
        if ("ALL".equals(filter) || filter.isBlank()) {
            return true;
        }
        if ("RESOLVED".equals(filter)) {
            return complaint.getStatus() == ComplaintStatus.RESOLVED;
        }
        if ("UNRESOLVED".equals(filter)) {
            return complaint.getStatus() != ComplaintStatus.RESOLVED;
        }
        if ("ESCALATED".equals(filter)) {
            return isEscalatedComplaint(complaint);
        }
        if ("QUEUE".equals(filter)) {
            return historyRepository.findByComplaintId(complaint.getId()).isEmpty();
        }
        if ("ASSIGNED".equals(filter)) {
            return isAssignedToTechnician(complaint);
        }
        return true;
    }

    private boolean isEscalatedComplaint(Complaint complaint) {
        return historyRepository.findByComplaintId(complaint.getId()).stream()
            .anyMatch(h -> {
                String note = h.getNote();
                return note != null && note.startsWith("ESCALATED_TO_");
            });
    }

    private boolean isAssignedToTechnician(Complaint complaint) {
        return historyRepository.findByComplaintId(complaint.getId()).stream()
            .anyMatch(h -> {
                String note = h.getNote();
                return note != null && note.contains("TECHNICIAN");
            });
    }

    private List<Complaint> complaintsVisibleToRole(UUID userId, UserRole role) {
        if (role == UserRole.CUSTOMER) {
            Consumer consumer = consumerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No consumer linked to customer user."));
            return complaintRepository.findByConsumerId(consumer.getId());
        }

        User user = getUserOrThrow(userId);
        UUID regionId = user.getRegion() != null ? user.getRegion().getId() : null;
        if (regionId == null) {
            return List.of();
        }
        return complaintRepository.findByRegionId(regionId);
    }

    private Complaint getComplaintOrThrow(UUID complaintId) {
        return complaintRepository.findById(complaintId)
            .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}
