package com.electricity.cms.model;
import jakarta.persistence.*;
import java.util.UUID;
@Entity
@Table(name = "divisions")
public class Division {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;
    public Division() {}
    public UUID   getId()           { return id; }
    public String getName()         { return name; }
    public void   setName(String v) { this.name = v; }
    @Override
    public String toString() { return "Division{id=" + id + ", name='" + name + "'}"; }
}
