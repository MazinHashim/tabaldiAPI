package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Session;
import com.tabaldi.api.model.UserEntity;
import com.tabaldi.api.model.UserVerification;
import com.tabaldi.api.payload.SendOtpPayload;
import com.tabaldi.api.payload.VerifyOtpPayload;
import com.tabaldi.api.response.AuthenticationResponse;
import com.tabaldi.api.response.SendOtpResponse;
import com.tabaldi.api.response.UserResponse;
import com.tabaldi.api.response.VerificationResponse;
import com.tabaldi.api.service.UserService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final MessageSource messageSource;

    @PostMapping("/send/otp")
    public @ResponseBody ResponseEntity<SendOtpResponse> sendOtp (
            @Valid @RequestBody SendOtpPayload payload
    ) throws TabaldiGenericException {
        Locale locale = LocaleContextHolder.getLocale();
        String sentMessage = messageSource.getMessage("success.otp.send",null, locale);
        UserVerification createdVerification = userService.sendOtp(payload);

        Instant pointInTime = Instant.ofEpochMilli(createdVerification.getExpiryTime().toEpochSecond());
        String expireOn = OffsetDateTime.ofInstant(pointInTime, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return ResponseEntity.ok(SendOtpResponse.builder()
                .phone(createdVerification.getPhone())
                .expireOn(expireOn)
                .keyRef(createdVerification.getKeyRef()+" "+createdVerification.getCode())// remove code in production
                .message(sentMessage)
                .build());
    }

    @PostMapping("/verify/otp/login")
    public @ResponseBody ResponseEntity<AuthenticationResponse> verifyOtpAndLogin (
            @RequestBody @Valid VerifyOtpPayload payload
    ) throws TabaldiGenericException {
        VerificationResponse userVerificationResponse = userService.verifyOtp(payload);
        UserVerification userVerification = userVerificationResponse.getUserVerification();
        Session loginSession = userService.login(payload, userVerification);

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(loginSession.getSessionToken())
                .refreshToken(loginSession.getRefreshToken())
                .newUser(userVerificationResponse.isNewUser()) // this for customer to add additional info
                .role( userVerification.getUser()!=null
                        ? userVerification.getUser().getRole().name()
                        : null) // this for super admin and vendor for redirect to authorized route
                .message(messageSource.getMessage("success.login",null, LocaleContextHolder.getLocale()))
                .build());
    }
    @PostMapping("/verify/otp")
    public @ResponseBody ResponseEntity<VerificationResponse> verifyOtp (
            @RequestBody @Valid VerifyOtpPayload payload
    ) throws TabaldiGenericException {
        VerificationResponse userVerificationResponse = userService.verifyOtp(payload);
        userVerificationResponse.setMessage(messageSource.getMessage("success.phone.verification",null,
                LocaleContextHolder.getLocale()));
        return ResponseEntity.ok(userVerificationResponse);
    }

    @GetMapping("/logout")
    public @ResponseBody ResponseEntity<AuthenticationResponse> logout () throws TabaldiGenericException {
        Session logoutSession = userService.logout();
        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .token(logoutSession.getSessionToken())
                        .refreshToken(logoutSession.getRefreshToken())
                        .message(messageSource.getMessage("success.logout",null, LocaleContextHolder.getLocale()))
                        .build()
        );
    }

    @GetMapping("/refresh/{refreshToken}")
    public @ResponseBody ResponseEntity<AuthenticationResponse> refresh (
            @PathVariable @Valid String refreshToken
    ) throws TabaldiGenericException {
        Session refreshSession = userService.refreshSessionToken(refreshToken);

        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .token(refreshSession.getSessionToken())
                        .refreshToken(refreshSession.getRefreshToken())
                        .message(messageSource.getMessage("success.session.refresh",null, LocaleContextHolder.getLocale()))
                        .build()
        );
    }

    @GetMapping("/profile")
    public @ResponseBody ResponseEntity<UserResponse> profile (
            @RequestParam(value = "vendorUserId", required = false) Long vendorUserId) throws TabaldiGenericException {
        UserEntity user = userService.getProfile(vendorUserId); // if vendorUserId is null will fetch super admin data
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "User", "المستخدم");
        return ResponseEntity.ok(
                UserResponse.builder()
                        .newUser(!userService.checkUserExistRegardlessOfRole(user))
                        .user(user)
                        .message(fetchMessage)
                        .build()
        );
    }
}
