package com.nnenna.portal.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CustomerController {

    public record CustomerRequest(@NotBlank String fullName, @Email @NotBlank String email,
                                  String phone, String address) {}

    public record CustomerView(Long id, String fullName, String email, String phone, String address) {
        static CustomerView of(Customer c) {
            return new CustomerView(c.getId(), c.getFullName(), c.getEmail(), c.getPhone(), c.getAddress());
        }
    }

    private final CustomerRepository customers;

    public CustomerController(CustomerRepository customers) {
        this.customers = customers;
    }

    @PostMapping("/customers/me")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerView create(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CustomerRequest r) {
        if (customers.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists");
        }
        return CustomerView.of(customers.save(new Customer(userId, r.fullName(), r.email(), r.phone(), r.address())));
    }

    @GetMapping("/customers/me")
    public CustomerView me(@RequestHeader("X-User-Id") Long userId) {
        return CustomerView.of(find(userId));
    }

    @PutMapping("/customers/me")
    public CustomerView update(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CustomerRequest r) {
        Customer c = find(userId);
        c.update(r.fullName(), r.email(), r.phone(), r.address());
        return CustomerView.of(customers.save(c));
    }

    @GetMapping("/customers")
    public List<CustomerView> all(@RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return customers.findAll().stream().map(CustomerView::of).toList();
    }

    @GetMapping("/customers/{id}")
    public CustomerView byId(@PathVariable Long id, @RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return CustomerView.of(find(id));
    }

    /** Service-to-service lookup. Not exposed by the gateway. */
    @GetMapping("/internal/customers/{id}")
    public CustomerView internal(@PathVariable Long id) {
        return CustomerView.of(find(id));
    }

    private Customer find(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }
}
