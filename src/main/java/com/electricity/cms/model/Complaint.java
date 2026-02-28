package com.electricity.cms.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "complaints")
public class Complaint {
    public enum ComplaintStatus { PENDING, IN_PROGRESS, RESOLVED, REJECTED }
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    /** Always set — even walk-in non-registered complainants have a person record. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
    /** Nullable — complaint may come from a non-registered consumer. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;
    /** Nullable — only set when complaint is bill-related. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;
    @Column(name = "subject", length = 255)
    private String subject;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "evidence")
    private byte[] evidence;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "complaint_status")
    private ComplaintStatus status = ComplaintStatus.PENDING;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    public Complaint() {}
    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
    public UUID            getId()                       { return id; }
    public Person          getPerson()                   { return person; }
    public void            setPerson(Person v)           { this.person = v; }
    public Consumer        getConsumer()                 { return consumer; }
    public void            setConsumer(Consumer v)       { this.consumer = v; }
    public Bill            getBill()                     { return bill; }
    public void            setBill(Bill v)               { this.bill = v; }
    public String          getSubject()                  { return subject; }
    public void            setSubject(String v)          { this.subject = v; }
    public String          getDescription()              { return description; }
    public void            setDescription(String v)      { this.description = v; }
    public byte[]          getEvidence()                 { return evidence; }
    public void            setEvidence(byte[] v)         { this.evidence = v; }
    public ComplaintStatus getStatus()                   { return status; }
    public void            setStatus(ComplaintStatus v)  { this.status = v; }
    public LocalDateTime   getCreatedAt()                { return createdAt; }
    public LocalDateTime   getUpdatedAt()                { return updatedAt; }
    public void            setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
    @Override
    public String toString() {
        return "Complaint{id=" + id + ", status=" + status + ", subject='" + subject + "'}";
    }
}
