package com.electricity.cms.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "consumers")
public class Consumer {
    public enum ConnectionType { RESIDENT, COMMERCIAL }
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
    @Column(name = "consumer_id", unique = true, length = 50)
    private String consumerId;
    @Column(name = "bill_reference", unique = true, length = 20)
    private String billReference;
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false, columnDefinition = "conn_type")
    private ConnectionType connectionType = ConnectionType.RESIDENT;
    @Column(name = "installation_date")
    private LocalDate installationDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private AdministrativeZone zone;
    @Column(name = "current_address", columnDefinition = "TEXT")
    private String currentAddress;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    public Consumer() {}
    public UUID            getId()                            { return id; }
    public Person          getPerson()                        { return person; }
    public void            setPerson(Person v)                { this.person = v; }
    public String          getConsumerId()                    { return consumerId; }
    public void            setConsumerId(String v)            { this.consumerId = v; }
    public String          getBillReference()                 { return billReference; }
    public void            setBillReference(String v)         { this.billReference = v; }
    public ConnectionType  getConnectionType()                { return connectionType; }
    public void            setConnectionType(ConnectionType v){ this.connectionType = v; }
    public LocalDate       getInstallationDate()              { return installationDate; }
    public void            setInstallationDate(LocalDate v)   { this.installationDate = v; }
    public AdministrativeZone getZone()                       { return zone; }
    public void            setZone(AdministrativeZone v)      { this.zone = v; }
    public String          getCurrentAddress()                { return currentAddress; }
    public void            setCurrentAddress(String v)        { this.currentAddress = v; }
    public LocalDateTime   getUpdatedAt()                     { return updatedAt; }
    public void            setUpdatedAt(LocalDateTime v)      { this.updatedAt = v; }
    @Override
    public String toString() {
        return "Consumer{id=" + id + ", consumerId='" + consumerId + "'}";
    }
}
