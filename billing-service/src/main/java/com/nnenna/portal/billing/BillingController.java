package com.nnenna.portal.billing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/billing/invoices")
public class BillingController {

    public record CreateInvoiceRequest(@NotNull Long customerId,
                                       @NotNull @DecimalMin("0.01") BigDecimal amount,
                                       String description) {}

    public record InvoiceView(Long id, Long customerId, BigDecimal amount, String description,
                              InvoiceStatus status, Instant createdAt, Instant paidAt) {
        static InvoiceView of(Invoice i) {
            return new InvoiceView(i.getId(), i.getCustomerId(), i.getAmount(), i.getDescription(),
                    i.getStatus(), i.getCreatedAt(), i.getPaidAt());
        }
    }

    private final BillingService service;

    public BillingController(BillingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceView create(@RequestHeader("X-User-Role") String role, @Valid @RequestBody CreateInvoiceRequest r) {
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
        return InvoiceView.of(service.create(r.customerId(), r.amount(), r.description()));
    }

    @GetMapping
    public List<InvoiceView> list(@RequestHeader("X-User-Id") Long userId, @RequestHeader("X-User-Role") String role) {
        return service.list(userId, role).stream().map(InvoiceView::of).toList();
    }

    @GetMapping("/{id}")
    public InvoiceView get(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId,
                           @RequestHeader("X-User-Role") String role) {
        return InvoiceView.of(service.get(id, userId, role));
    }

    @PostMapping("/{id}/pay")
    public InvoiceView pay(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        return InvoiceView.of(service.pay(id, userId));
    }
}
