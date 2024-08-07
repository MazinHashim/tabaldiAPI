package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.model.OrderStatus;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.payload.*;
import com.tabaldi.api.response.AdminHomeDetails;
import com.tabaldi.api.response.OrderMapper;
import com.tabaldi.api.response.VendorHomeDetails;
import com.tabaldi.api.service.*;
import com.tabaldi.api.utils.HttpHeadersUtils;
import com.tabaldi.api.utils.RestUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final TabaldiConfiguration configuration;

    @Override
    public Map<String, Object> initializeMyFatoorahPayment(InitPaymentPayload initPaymentPayload) throws TabaldiGenericException {
        return callMyfatoorahPaymentAPI("/InitiatePayment", initPaymentPayload);
    }
    @Override
    public Map<String, Object> executePaymentTransaction(ExecutePaymentPayload executePaymentPayload) throws TabaldiGenericException {
        return callMyfatoorahPaymentAPI("/ExecutePayment", executePaymentPayload);
    }
    @Override
    public Map<String, Object> directPaymentTransaction(DirectPaymentPayload directPaymentPayload, String paymentURL) throws TabaldiGenericException {
        try {
            URI uri = new URI(paymentURL);
            String path = uri.getPath();
            String[] segments = path.split("/");
            String invoiceKey = segments[segments.length - 2];
            String paymentGatewayId = segments[segments.length - 1];

            return callMyfatoorahPaymentAPI("/DirectPayment/"+invoiceKey+"/"+paymentGatewayId, directPaymentPayload);
        } catch (URISyntaxException e) {e.printStackTrace(); return null;}
    }

    private <T> Map<String, Object> callMyfatoorahPaymentAPI(String endpoint,T paymentPayload) throws TabaldiGenericException {
        HttpHeaders payloadHeaders = HttpHeadersUtils.getApplicationJsonHeader();
        payloadHeaders.setBearerAuth(configuration.getMyfatoorahApiTestKey());

        HttpEntity<T> requestHttp =
                new HttpEntity<>(paymentPayload, payloadHeaders);
        String url = configuration.getMyfatoorahTestBaseUrl()+endpoint;
        Map<String, Object> apiResponse = RestUtils.postRequest(url, requestHttp, HashMap.class,
                HttpServletResponse.SC_BAD_REQUEST, "Failed");
        if(Boolean.valueOf(apiResponse.get("IsSuccess").toString())){
            return apiResponse;
        } else {
            List<Map> errors = (ArrayList) apiResponse.get("ValidationErrors");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, errors.get(0).get("Error").toString());
        }
    }
}
