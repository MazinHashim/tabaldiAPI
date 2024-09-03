package com.tabaldi.api.utils;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.serviceImpl.PaymentServiceImpl;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@NoArgsConstructor
public class RestUtils {
    private static final RestTemplate restTemplate = new RestTemplate();
    final static Logger logger = LoggerFactory.getLogger(RestUtils.class);

    public static <T> T postRequest(String url, @Nullable Object request, Class<T> responseType, int exceptionCode, String exceptionMessage) throws HttpClientErrorException, TabaldiGenericException {
        ResponseEntity<T> entity = restTemplate.postForEntity(url, request, responseType, new Object[0]);
        if (entity.getStatusCode().equals(HttpStatus.OK) && entity.getBody() != null) {
            return entity.getBody();
        } else {
            logger.error(entity.getBody().toString());
            throw new TabaldiGenericException(exceptionCode, exceptionMessage);
        }
    }

    public static <T> T getRequest(String url, @Nullable Object request, Class<T> responseType, int exceptionCode, String exceptionMessage) throws HttpClientErrorException, TabaldiGenericException {
        ResponseEntity<T> entity = restTemplate.getForEntity(url, responseType, request, new Object[0]);
        if (entity.getStatusCode().equals(HttpStatus.OK) && entity.getBody() != null) {
            return entity.getBody();
        } else {
            throw new TabaldiGenericException(exceptionCode, exceptionMessage);
        }
    }
}
