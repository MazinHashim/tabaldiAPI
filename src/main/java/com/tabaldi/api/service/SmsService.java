package com.tabaldi.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tabaldi.api.exception.TabaldiGenericException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface SmsService {
    String sendSms(String to, String text) throws TabaldiGenericException, UnsupportedEncodingException;
    String getJwtToken(boolean useShortLivedToken) throws TabaldiGenericException, JsonProcessingException;
}
