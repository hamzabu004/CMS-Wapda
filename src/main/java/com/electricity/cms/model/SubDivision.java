package com.electricity.cms.model;
import jakarta.persistence.*;
import java.util.UUID;
@Entity
@Table(name = "sub_divisions")
public class SubDivision {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    public SubDivision() {}
    public UUID     getId()                { return id; }
    public Division getDivision()          { return division; }
    public void     setDivision(Division v){ this.division = v; }
    public String   getName()              { return name; }
    public void     setName(String v)      { this.name = v; }
    @Override
    public String toString() { return "SubDivision{id=" + id + ", name='" + name + "'}"; }
}
