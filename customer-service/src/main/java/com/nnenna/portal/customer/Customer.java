package com.nnenna.portal.customer;

import jakarta.persistence.*;
import java.time.Instant;

/** The customer id is the auth-service user id, so no generator is used. */
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String address;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Customer() {
    }

    public Customer(Long id, String fullName, String email, String phone, String address) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public void update(String fullName, String email, String phone, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public Instant getCreatedAt() { return createdAt; }
}
