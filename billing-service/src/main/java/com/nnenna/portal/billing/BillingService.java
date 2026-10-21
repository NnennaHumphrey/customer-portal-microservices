package com.nnenna.portal.billing;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import feign.FeignException;

@Service
public class BillingService {

    private final InvoiceRepository invoices;
    private final CustomerClient customers;
    private final NotificationClient notifications;

    public BillingService(InvoiceRepository invoices, CustomerClient customers, NotificationClient notifications) {
        this.invoices = invoices;
        this.customers = customers;
        this.notifications = notifications;
    }

    public Invoice create(Long customerId, BigDecimal amount, String description) {
        try {
            customers.get(customerId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Customer service unavailable");
        }
        Invoice saved = invoices.save(new Invoice(customerId, amount, description));
        notifications.send(new NotificationRequest(customerId,
                "Invoice #" + saved.getId() + " issued for " + saved.getAmount()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Invoice> list(Long userId, String role) {
        return "ADMIN".equals(role) ? invoices.findAll() : invoices.findByCustomerIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Invoice get(Long id, Long userId, String role) {
        return invoices.findById(id)
                .filter(i -> "ADMIN".equals(role) || i.getCustomerId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    }

    @Transactional
    public Invoice pay(Long id, Long userId) {
        Invoice invoice = invoices.findById(id)
                .filter(i -> i.getCustomerId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice already paid");
        }
        invoice.markPaid();
        notifications.send(new NotificationRequest(userId, "Payment received for invoice #" + id));
        return invoice;
    }
}
