package com.tabaldi.api.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.service.SmsService;
import com.tabaldi.api.utils.HttpHeadersUtils;
import com.tabaldi.api.utils.RestUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final TabaldiConfiguration configuration;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String getJwtToken(boolean useShortLivedToken) throws TabaldiGenericException, JsonProcessingException {

        // Create the request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("command", "auth");
        requestBody.put("username", configuration.getSmsGatewayUsername());
        requestBody.put("password", configuration.getSmsGatewayPassword());
        HttpHeaders headers = HttpHeadersUtils.getApplicationJsonHeader();

        // Create the request entity
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send the POST request
        String response = RestUtils.postRequest(configuration.getSmsGatewayEndpointUrl()+"/api_jwt.php",
                requestEntity, String.class, HttpServletResponse.SC_BAD_REQUEST, "Failed");

        // Parse the response JSON
        JsonNode root = objectMapper.readTree(response);
        String tokenType = useShortLivedToken ? "token_short" : "token_long";
        String token = root.path("answer").path(tokenType).path("jwt").asText();

        return token;
    }
    public String sendSms(String to, String text) throws TabaldiGenericException, UnsupportedEncodingException {

//        // First, get the JWT token (short-lived or long-lived as per requirement)
//        String token = getJwtToken(true); // Set to true for short-lived, false for long-lived

        // Ensure the text is properly URL encoded
        String encodedText = URLEncoder.encode(text, "UTF-8");

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
                configuration.getSmsGatewayEndpointUrl()+"/api_http.php")
                .queryParam("username", configuration.getSmsGatewayUsername())
                .queryParam("password", configuration.getSmsGatewayPassword())
                .queryParam("senderid", configuration.getSmsGatewaySenderId())
                .queryParam("to", to)
                .queryParam("text", encodedText)
                .queryParam("type", "text");

        String fullUrl = uriBuilder.toUriString();

        // Add the JWT token in the Authorization header
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(token); // Set the JWT token in the Authorization header
//        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Send the request and get the response
        return RestUtils.getRequest(fullUrl, null, String.class , HttpServletResponse.SC_BAD_REQUEST, "Failed");
    }
}
