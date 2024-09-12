package com.tabaldi.api;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@RequiredArgsConstructor
@EnableConfigurationProperties(TabaldiConfiguration.class)
public class TabaldiApiApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(TabaldiApiApplication.class, args);
	}
	@Override
	public void run(String... args) throws Exception {
//		emailService.sendEmail("maz.hash03@gmail.com", "Test Message", "This is first test message");
//	vendorService.getVendorProductsList(15L).forEach(product -> {
//		System.out.println(product.getCategory().getVendor().getFullName());
//	});
//		sequencesService.createSequenceFor("customer", 1000, 2);
//		sequencesService.createSequenceFor("customer", 1000, 3);
//		sequencesService.getNextSequenceFor("customer", 2);
//		sequencesService.getNextSequenceFor("customer", 2);
//		sequencesService.getNextSequenceFor("customer", 2);
//		sequencesService.getNextSequenceFor("customer", 2);
//		sequencesService.getNextSequenceFor("customer", 3);
//		sequencesService.getNextSequenceFor("customer", 3);
//		sequencesService.getNextSequenceFor("customer", 3);
	}
}
