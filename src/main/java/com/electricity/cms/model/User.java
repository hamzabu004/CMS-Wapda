package com.electricity.cms.model;
import jakarta.persistence.*;
import java.util.UUID;
@Entity
@Table(name = "users")
public class User {
    public enum Role { ADMIN, CONSUMER, TECHNICIAN, MANAGER, CUSTOMER_SERVICE }
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;
    @Column(name = "email", unique = true, length = 255)
    private String email;
    @Column(name = "password", length = 255)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "user_role")
    private Role role = Role.CONSUMER;
    public User() {}
    public UUID   getId()               { return id; }
    public Person getPerson()           { return person; }
    public void   setPerson(Person v)   { this.person = v; }
    public String getEmail()            { return email; }
    public void   setEmail(String v)    { this.email = v; }
    public String getPassword()         { return password; }
    public void   setPassword(String v) { this.password = v; }
    public Role   getRole()             { return role; }
    public void   setRole(Role v)       { this.role = v; }
    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role=" + role + "}";
    }
}
