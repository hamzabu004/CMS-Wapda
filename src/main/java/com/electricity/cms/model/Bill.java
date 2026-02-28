package com.electricity.cms.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "bills")
public class Bill {
    public enum BillStatus { PAID, UNPAID, OVERDUE, DISPUTED }
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private Consumer consumer;
    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;
    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;
    @Column(name = "units_consumed", precision = 10, scale = 2)
    private BigDecimal unitsConsumed;
    @Column(name = "amount_due", precision = 12, scale = 2)
    private BigDecimal amountDue;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "bill_status")
    private BillStatus status = BillStatus.UNPAID;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    public Bill() {}
    public UUID       getId()                            { return id; }
    public Consumer   getConsumer()                      { return consumer; }
    public void       setConsumer(Consumer v)            { this.consumer = v; }
    public LocalDate  getBillingPeriodStart()            { return billingPeriodStart; }
    public void       setBillingPeriodStart(LocalDate v) { this.billingPeriodStart = v; }
    public LocalDate  getBillingPeriodEnd()              { return billingPeriodEnd; }
    public void       setBillingPeriodEnd(LocalDate v)   { this.billingPeriodEnd = v; }
    public BigDecimal getUnitsConsumed()                 { return unitsConsumed; }
    public void       setUnitsConsumed(BigDecimal v)     { this.unitsConsumed = v; }
    public BigDecimal getAmountDue()                     { return amountDue; }
    public void       setAmountDue(BigDecimal v)         { this.amountDue = v; }
    public LocalDate  getDueDate()                       { return dueDate; }
    public void       setDueDate(LocalDate v)            { this.dueDate = v; }
    public BillStatus getStatus()                        { return status; }
    public void       setStatus(BillStatus v)            { this.status = v; }
    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
    public void       setUpdatedAt(LocalDateTime v)      { this.updatedAt = v; }
    @Override
    public String toString() {
        return "Bill{id=" + id + ", status=" + status + ", amountDue=" + amountDue + "}";
    }
}
