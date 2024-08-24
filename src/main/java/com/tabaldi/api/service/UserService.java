package com.tabaldi.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Session;
import com.tabaldi.api.model.UserEntity;
import com.tabaldi.api.model.UserVerification;
import com.tabaldi.api.payload.SendOtpPayload;
import com.tabaldi.api.payload.VerifyOtpPayload;
import com.tabaldi.api.response.VerificationResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public interface UserService {
    boolean checkUserExistRegardlessOfRole(UserEntity user);
    UserVerification sendOtp(@Valid SendOtpPayload payload) throws TabaldiGenericException, JsonProcessingException, UnsupportedEncodingException;
    VerificationResponse verifyOtp(@Valid VerifyOtpPayload payload) throws TabaldiGenericException;
    Session login(@Valid VerifyOtpPayload payload, UserVerification userVerification) throws TabaldiGenericException;
    Session logout() throws TabaldiGenericException;
    Boolean deleteUserById(Long userId) throws TabaldiGenericException;

    Session refreshSessionToken(String refreshToken) throws TabaldiGenericException;
    UserEntity getProfile(Long vendorUserId) throws TabaldiGenericException;
    UserEntity getUserById(Long userId) throws TabaldiGenericException;

    UserEntity changeUserPhoneNumber(long userId, String newPhoneNumber, VerifyOtpPayload payload) throws TabaldiGenericException;
}
