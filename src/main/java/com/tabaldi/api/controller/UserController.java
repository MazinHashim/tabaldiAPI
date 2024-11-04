package com.tabaldi.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Session;
import com.tabaldi.api.model.UserEntity;
import com.tabaldi.api.model.UserVerification;
import com.tabaldi.api.payload.SendOtpPayload;
import com.tabaldi.api.payload.UserPayload;
import com.tabaldi.api.payload.VerifyOtpPayload;
import com.tabaldi.api.response.*;
import com.tabaldi.api.service.UserService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/admin")
    public @ResponseBody ResponseEntity<ListResponse<UserEntity>> getUsersList () throws TabaldiGenericException {
        List<UserEntity> usersList = userService.getAdminUsersList();
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Users", "المستخدمين");
        return ResponseEntity.ok(
                ListResponse.<UserEntity>genericBuilder()
                        .list(usersList)
                        .message(fetchMessage)
                        .build()
        );
    }
    @PostMapping("/add")
    public @ResponseBody ResponseEntity<UserResponse> addUser (
            @RequestBody @Valid UserPayload payload
    ) throws TabaldiGenericException {
        String event = payload.getUserId()==null?"created":"updated";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "User", "المستخدم", event, event.equals("created")?"حفظ":"تعديل");
        UserEntity user = userService.addUser(payload);
        return ResponseEntity.ok(
                UserResponse.builder()
                        .user(user)
                        .message(successSaveMessage)
                        .build()
        );

    }

    @PostMapping("/send/otp")
    public @ResponseBody ResponseEntity<SendOtpResponse> sendOtp (
            @Valid @RequestBody SendOtpPayload payload
    ) throws TabaldiGenericException, JsonProcessingException, UnsupportedEncodingException {
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
                .userId(loginSession.getUser().getUserId())
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
    @PostMapping("/verify/changePhone/{userId}/{newPhoneNumber}")
    public @ResponseBody ResponseEntity<UserResponse> changeUserPhoneNumber (
            @PathVariable @Valid long userId,
            @PathVariable @Valid String newPhoneNumber,
            @RequestBody @Valid VerifyOtpPayload payload
    ) throws TabaldiGenericException {
        UserEntity user = userService.changeUserPhoneNumber(userId, newPhoneNumber, payload);
        return ResponseEntity.ok(UserResponse.builder()
                .user(user)
                .message(messageSource.getMessage("success.phone.changed",null,
                        LocaleContextHolder.getLocale()))
                .build());
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
    @DeleteMapping("/delete/{userId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteUser (
            @PathVariable Long userId) throws TabaldiGenericException {
        boolean isDeleted = userService.deleteUserById(userId);
        String deletedMessage = MessagesUtils.getDeletedMessage(messageSource, "User", "المستخدم");
        return ResponseEntity.ok(
                DeleteResponse.builder()
                        .isDeleted(isDeleted)
                        .message(deletedMessage)
                        .build()
        );
    }
}
