package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    public static final String UTF_8_ENCODING = "UTF-8";
    private final JavaMailSender emailSender;
    private final TabaldiConfiguration configuration;
    final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
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

    @Override
    public void sendEmailWithAttachment(String to, String subject, String message, String filePath) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8_ENCODING);
            logger.info("rateena app email "+configuration.getEmailUsername());
            helper.setFrom(configuration.getEmailUsername());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message);
            // Add Attachment
            String fileName = filePath.substring(filePath
                    .lastIndexOf(configuration.getActiveProfile().equals("local")?"\\":"/")-1);
            FileSystemResource fsr = new FileSystemResource(filePath);
            helper.addAttachment(fileName, fsr);
            logger.info("Message Sending...");
            this.emailSender.send(mimeMessage);
            logger.info("Message Sent Successfully...");
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }
}
