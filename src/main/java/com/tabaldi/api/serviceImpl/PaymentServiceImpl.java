package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.payload.*;
import com.tabaldi.api.service.*;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.HttpHeadersUtils;
import com.tabaldi.api.utils.RestUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final TabaldiConfiguration configuration;
    final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Override
    public Map<String, Object> initializeMyFatoorahPayment(InitPaymentPayload initPaymentPayload) throws TabaldiGenericException, IOException, HttpClientErrorException {
        return callMyfatoorahPaymentAPI("/InitiatePayment", initPaymentPayload);
    }
    @Override
    public Map<String, Object> executePaymentTransaction(ExecutePaymentPayload executePaymentPayload) throws TabaldiGenericException, IOException, HttpClientErrorException {
        return callMyfatoorahPaymentAPI("/ExecutePayment", executePaymentPayload);
    }
    @Override
    public Map<String, Object> directPaymentTransaction(DirectPaymentPayload directPaymentPayload, String paymentURL) throws TabaldiGenericException, IOException, HttpClientErrorException {
        try {
            URI uri = new URI(paymentURL);
            String path = uri.getPath();
            String[] segments = path.split("/");
            String invoiceKey = segments[segments.length - 2];
            String paymentGatewayId = segments[segments.length - 1];

            return callMyfatoorahPaymentAPI("/DirectPayment/"+invoiceKey+"/"+paymentGatewayId, directPaymentPayload);
        } catch (URISyntaxException e) {e.printStackTrace(); return null;}
    }

    private <T> Map<String, Object> callMyfatoorahPaymentAPI(String endpoint,T paymentPayload) throws TabaldiGenericException, IOException, HttpClientErrorException {
        HttpHeaders payloadHeaders = HttpHeadersUtils.getApplicationJsonHeader();
        payloadHeaders.setBearerAuth(configuration.getMyfatoorahApiTestKey());

        HttpEntity<T> requestHttp =
                new HttpEntity<>(paymentPayload, payloadHeaders);
        String url = configuration.getMyfatoorahTestBaseUrl()+endpoint;
        logger.info(url);
//        try {
        String strResponse = RestUtils.postRequest(url, requestHttp, String.class,
                HttpServletResponse.SC_BAD_REQUEST, "Failed");
        logger.info(strResponse);
        return GenericMapper.jsonToObjectMapper(strResponse, Map.class);
//        } catch (HttpClientErrorException ex) {
            // delete order if There was any error happen (maybe not required)
//            Map<String, Object> apiResponse = ex.getResponseBodyAs(Map.class);
//            if(!Boolean.valueOf(apiResponse.get("IsSuccess").toString())){
//            Map<String, Object> directData = (HashMap) apiResponse.get("Data");
//            String errorMessage = directData.get("ErrorMessage").toString();
//            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
//            List<Map> errors = (ArrayList) apiResponse.get("ValidationErrors");
//            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, errors.get(0).get("Error").toString());
//            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, "Testing Error");
//        }
    }
}
