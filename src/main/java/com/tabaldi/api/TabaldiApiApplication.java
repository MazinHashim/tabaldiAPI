package com.tabaldi.api;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.payload.InitPaymentPayload;
import com.tabaldi.api.service.SequencesService;
import com.tabaldi.api.service.VendorService;
import com.tabaldi.api.utils.HttpHeadersUtils;
import com.tabaldi.api.utils.RestUtils;
import com.twilio.Twilio;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RequiredArgsConstructor
@EnableConfigurationProperties(TabaldiConfiguration.class)
public class TabaldiApiApplication implements CommandLineRunner {

	private final VendorService vendorService;
	private final SequencesService sequencesService;
	private final TabaldiConfiguration configuration;
	public static void main(String[] args) {
		SpringApplication.run(TabaldiApiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Twilio.init(configuration.getTwilioAccountSid(), configuration.getTwilioAuthToken());

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
