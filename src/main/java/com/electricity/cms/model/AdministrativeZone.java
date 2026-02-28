package com.electricity.cms.model;
import jakarta.persistence.*;
import java.util.UUID;
@Entity
@Table(name = "administrative_zones")
public class AdministrativeZone {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_division_id", nullable = false)
    private SubDivision subDivision;
    @Column(name = "city", length = 50)
    private String city;
    public AdministrativeZone() {}
    public UUID        getId()                       { return id; }
    public SubDivision getSubDivision()              { return subDivision; }
    public void        setSubDivision(SubDivision v) { this.subDivision = v; }
    public String      getCity()                     { return city; }
    public void        setCity(String v)             { this.city = v; }
    @Override
    public String toString() { return "AdministrativeZone{id=" + id + ", city='" + city + "'}"; }
}
