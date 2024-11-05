package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.payload.DirectPaymentPayload;
import com.tabaldi.api.payload.ExecutePaymentPayload;
import com.tabaldi.api.payload.InitPaymentPayload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Map;

@Service
public interface PaymentService {
    Map<String, Object> initializeMyFatoorahPayment(InitPaymentPayload payload) throws TabaldiGenericException, IOException, HttpClientErrorException;

    Map<String, Object> executePaymentTransaction(ExecutePaymentPayload payload) throws TabaldiGenericException, IOException, HttpClientErrorException;
    Map<String, Object> directPaymentTransaction(DirectPaymentPayload payload, String paymentURL) throws TabaldiGenericException, IOException, HttpClientErrorException;
}
