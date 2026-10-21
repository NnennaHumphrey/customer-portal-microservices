package com.nnenna.portal.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant paidAt;

    protected Invoice() {
    }

    public Invoice(Long customerId, BigDecimal amount, String description) {
        this.customerId = customerId;
        this.amount = amount;
        this.description = description;
    }

    public void markPaid() {
        this.status = InvoiceStatus.PAID;
        this.paidAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public InvoiceStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
}
