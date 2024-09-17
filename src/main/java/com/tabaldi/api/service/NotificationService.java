package com.tabaldi.api.service;

import com.tabaldi.api.payload.NotificationPayload;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {

    void sendPushNotificationByToken(NotificationPayload payload);
}
