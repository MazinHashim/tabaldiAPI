package com.tabaldi.api.serviceImpl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.tabaldi.api.payload.NotificationPayload;
import com.tabaldi.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final FirebaseMessaging firebaseMessaging;


    @Override
    public void sendPushNotificationByToken(NotificationPayload payload) {

        try {
            firebaseMessaging.send(Message.builder()
                .setToken(payload.getToken())
                .setNotification(Notification.builder()
                    .setTitle(payload.getTitle())
                    .setBody(payload.getBody())
//                    .setImage(payload.getImage())
                    .build())
//                .putAllData(payload.getData())
            .build());
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
