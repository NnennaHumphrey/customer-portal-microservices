package com.nnenna.portal.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock InvoiceRepository invoices;
    @Mock CustomerClient customers;
    @Mock NotificationClient notifications;
    @InjectMocks BillingService service;

    @Test
    void payMarksInvoicePaidAndNotifiesCustomer() {
        Invoice invoice = new Invoice(5L, new BigDecimal("20.00"), "Plan");
        when(invoices.findById(1L)).thenReturn(Optional.of(invoice));

        Invoice paid = service.pay(1L, 5L);

        assertEquals(InvoiceStatus.PAID, paid.getStatus());
        verify(notifications).send(any(NotificationRequest.class));
    }

    @Test
    void payRejectsInvoiceOwnedBySomeoneElse() {
        Invoice invoice = new Invoice(5L, new BigDecimal("20.00"), "Plan");
        when(invoices.findById(1L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.pay(1L, 99L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void createReturns503WhenCustomerServiceFails() {
        when(customers.get(9L)).thenThrow(new RuntimeException("connection refused"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(9L, new BigDecimal("10.00"), "x"));

        assertEquals(503, ex.getStatusCode().value());
        verify(invoices, never()).save(any());
    }
}
