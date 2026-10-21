package com.nnenna.portal.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class NotificationController {

    public record NotificationRequest(@NotNull Long customerId, @NotBlank String message) {}

    public record NotificationView(Long id, String message, java.time.Instant createdAt) {}

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    /** Called by other services. Not exposed by the gateway. */
    @PostMapping("/internal/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public void send(@Valid @RequestBody NotificationRequest request) {
        repository.save(new Notification(request.customerId(), request.message()));
    }

    @GetMapping("/notifications")
    public List<NotificationView> mine(@RequestHeader("X-User-Id") Long userId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> new NotificationView(n.getId(), n.getMessage(), n.getCreatedAt()))
                .toList();
    }
}
