package com.nnenna.portal.notification;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Notification() {
    }

    public Notification(Long customerId, String message) {
        this.customerId = customerId;
        this.message = message;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
