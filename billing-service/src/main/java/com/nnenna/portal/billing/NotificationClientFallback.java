package com.nnenna.portal.billing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClientFallback implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientFallback.class);

    @Override
    public void send(NotificationRequest request) {
        log.warn("Notification service unavailable, dropped notification for customer {}", request.customerId());
    }
}
