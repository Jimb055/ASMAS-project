package com.asmas.notificationservice.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void handleEvent(String eventType, String payload) {
        System.out.println("Sending notification for event: " + eventType);
        System.out.println("Payload: " + payload);
    }
}
