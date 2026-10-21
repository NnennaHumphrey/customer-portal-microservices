package com.nnenna.portal.billing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Notifications are best-effort: if the service is down the fallback just logs. */
@FeignClient(name = "notification-service", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/internal/notifications")
    void send(@RequestBody NotificationRequest request);
}
