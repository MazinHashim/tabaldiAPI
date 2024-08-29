package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final TabaldiConfiguration configuration;

    @Override
    public void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            System.out.println(configuration.getEmailUsername());
            simpleMailMessage.setFrom(configuration.getEmailUsername());
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);
            System.out.println("Message Sending...");
            this.emailSender.send(simpleMailMessage);
            System.out.println("Message Sent Successfully...");
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }
}
