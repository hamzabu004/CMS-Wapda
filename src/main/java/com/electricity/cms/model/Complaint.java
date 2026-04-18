package com.electricity.cms.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "consumer_id", nullable = false)
    private Consumer consumer;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @Column(name = "customer_blocked", nullable = false)
    private boolean customerBlocked = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Enumerated(EnumType.STRING)
    private UserRole lastSenderRole;

    private boolean customerBlocked;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastUpdated == null) {
            lastUpdated = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }


    public boolean isCustomerBlocked() {
        return customerBlocked;
    }

    public void setCustomerBlocked(boolean customerBlocked) {
        this.customerBlocked = customerBlocked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public UserRole getLastSenderRole() { return lastSenderRole; }
    public void setLastSenderRole(UserRole role) { this.lastSenderRole = role; }

    public boolean isCustomerBlocked() { return customerBlocked; }
    public void setCustomerBlocked(boolean val) { this.customerBlocked = val; }
}
