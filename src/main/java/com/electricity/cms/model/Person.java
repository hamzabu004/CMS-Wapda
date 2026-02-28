package com.electricity.cms.model;
import jakarta.persistence.*;
import java.util.UUID;
@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @Column(name = "cnic", unique = true, nullable = false, length = 15)
    private String cnic;
    @Column(name = "full_name", length = 100)
    private String fullName;
    @Column(name = "phone_number", length = 15)
    private String phoneNumber;
    public Person() {}
    public UUID   getId()                  { return id; }
    public String getCnic()                { return cnic; }
    public void   setCnic(String v)        { this.cnic = v; }
    public String getFullName()            { return fullName; }
    public void   setFullName(String v)    { this.fullName = v; }
    public String getPhoneNumber()         { return phoneNumber; }
    public void   setPhoneNumber(String v) { this.phoneNumber = v; }
    @Override
    public String toString() {
        return "Person{id=" + id + ", cnic='" + cnic + "', fullName='" + fullName + "'}";
    }
}
