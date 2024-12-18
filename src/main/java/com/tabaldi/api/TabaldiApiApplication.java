package com.tabaldi.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tabaldi.api.payload.InitPaymentPayload;
import com.tabaldi.api.service.EmailService;
import com.tabaldi.api.service.PaymentService;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@SpringBootApplication
@RequiredArgsConstructor
@EnableConfigurationProperties(TabaldiConfiguration.class)
public class TabaldiApiApplication implements CommandLineRunner {
	// private final EmailService emailService;
	 private final PaymentService paymentService;

	@Bean
	FirebaseMessaging firebaseMessaging() throws IOException {
		ClassPathResource serviceAccount = new ClassPathResource(
				"rateena-cf40d-firebase-adminsdk-x3a5o-3c1068eec5.json");

		FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
				.build();

		return FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options));

	}

	public static void main(String[] args) {
		SpringApplication.run(TabaldiApiApplication.class, args);
		System.out.println("Rateena API Application started");
	}

	@Override
	public void run(String... args) throws Exception {
		// String pathURL = configuration.getInvoicePdfFolder() + "invoice_" + 4303866 +
		// ".pdf";
		// emailService.sendEmailWithAttachment(
		// "maz05@gmail.com", "Test Message", "This is first test message",
		// pathURL
		// );
		// notificationService.sendPushNotificationByToken(NotificationPayload.builder()
		// .token("")
		// .title("Rateena test Notification")
		// .body("This is awesome service to send push notification")
		// .build());
		// vendorService.getVendorProductsList(15L).forEach(product -> {
		// System.out.println(product.getCategory().getVendor().getFullName());
		// });
		// sequencesService.createSequenceFor("customer", 1000, 2);
		// sequencesService.createSequenceFor("customer", 1000, 3);
		// sequencesService.getNextSequenceFor("customer", 2);
		// sequencesService.getNextSequenceFor("customer", 2);
		// sequencesService.getNextSequenceFor("customer", 2);
		// sequencesService.getNextSequenceFor("customer", 2);
		// sequencesService.getNextSequenceFor("customer", 3);
		// sequencesService.getNextSequenceFor("customer", 3);
		// sequencesService.getNextSequenceFor("customer", 3);
		InitPaymentPayload initPaymentPayload = InitPaymentPayload.builder()
				.InvoiceAmount(100)
				.CurrencyIso("AED")
				.build();
		paymentService.initializeMyFatoorahPayment(initPaymentPayload);
	}
}
