package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.SendOtpPayload;
import com.tabaldi.api.payload.SessionPayload;
import com.tabaldi.api.payload.VerifyOtpPayload;
import com.tabaldi.api.repository.*;
import com.tabaldi.api.response.VerificationResponse;
import com.tabaldi.api.security.JwtService;
import com.tabaldi.api.service.SessionService;
import com.tabaldi.api.service.UserService;
import com.tabaldi.api.utils.MessagesUtils;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserVerificationRepository userVerificationRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final SessionRepository sessionRepository;
    private final MessageSource messageSource;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final TabaldiConfiguration configuration;
    @Override
    public UserVerification sendOtp(SendOtpPayload payload) throws TabaldiGenericException {
        Pattern pattern = Pattern.compile("05+\\d{8}");
        boolean matches;
        Matcher mat = pattern.matcher(payload.getPhone());
        matches = mat.matches();
        if(!matches){
            String phoneInvalidMessage = MessagesUtils.getInvalidFormatMessage(messageSource,
                     payload.getPhone(), payload.getPhone(),"phone", "رقم الهاتف");

            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, phoneInvalidMessage);
        }
        Optional<UserEntity> userOptional = userRepository.findByPhone(payload.getPhone());
        if(!userOptional.isPresent() && payload.isCheckExistence()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"User", "المستخدم");

            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Random random = new Random();
            int otpCode = random.ints(1111, 9999).findFirst().getAsInt();

            // get last active otp which will expire after expireDuration
            Optional<UserVerification> userVerification = userVerificationRepository.findLastSentCode(payload.getPhone());
            int resendTimes = 0;
            if (userVerification.isPresent()) {
                resendTimes = userVerification.get().getResendCounter();
                if (userVerification.get().getCreatedTime().isBefore(OffsetDateTime.now().minusHours(24))) {
                    resendTimes = 0;
                }
                if (resendTimes >= Integer.parseInt(configuration.getOtpResendTimesLimit())) {
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
                            messageSource.getMessage("error.exceed.resend.limit",null,
                                    LocaleContextHolder.getLocale()));
                }
                if (userVerification.get().getExpiryTime().isAfter(OffsetDateTime.now())) {
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
                            messageSource.getMessage("error.code.not.expired",null,
                                    LocaleContextHolder.getLocale()));
                }
            }

            String uuid = RandomString.make(64);
            UserVerification verification = UserVerification.builder()
                    .code(otpCode)
                    .phone(payload.getPhone())
                    .resendCounter(resendTimes + 1)
                    .createdTime(OffsetDateTime.now())
                    .expiryTime(OffsetDateTime.now().plusMinutes(Integer.parseInt(configuration.getOtpExpirationMin())))
                    .status(VerificationStatus.SENT)
                    .user(userOptional.isPresent() ? userOptional.get() : null)
                    .verifiedTime(null)
                    .keyRef(uuid)
                    .build();

            // send otp sms using twilio gateway
//            Message message = Message.creator(
//                            new com.twilio.type.PhoneNumber("whatsapp:+971553733809"),
//                            new com.twilio.type.PhoneNumber("MG15191d6b71b61e72c1e3445f65be8b01"),
//                            "HXfef4b2bd8f748aa05877d5a7adef65ff")
//                    .setContentVariables("{\"1\": \""+otpCode+"\"}")
//                    .create();
//            Message message = Message.creator(
//                    new com.twilio.type.PhoneNumber("whatsapp:+971553733809"),
//                    new com.twilio.type.PhoneNumber("whatsapp:+14155238886"), // 14127901077
//                    "Rateena welcome you, the otp for signin is: "+otpCode)
//            .create();
//            System.out.println(message.getStatus());
//            System.out.println(message.getErrorMessage());

            return userVerificationRepository.saveAndFlush(verification);
        }
    }

    @Override
    public VerificationResponse verifyOtp(VerifyOtpPayload payload) throws TabaldiGenericException {

        UserVerification userVerification =
                userVerificationRepository.findByPhoneAndCodeAndKeyRef(payload.getPhone(), payload.getOtpCode(),
                        payload.getKeyRef()).orElseThrow(()->{
                    String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Otp", "كود التحقق");

                    return new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
                });
        if (OffsetDateTime.now().isAfter(userVerification.getExpiryTime())
                || userVerification.getVerifiedTime() != null
                || userVerification.getStatus().equals(VerificationStatus.EXPIRED)) {
            userVerification.setStatus(VerificationStatus.EXPIRED);
            userVerificationRepository.save(userVerification);
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
                    messageSource.getMessage("error.verification.code.expired",null,
                            LocaleContextHolder.getLocale()));
        } else {
            // this code to check if there is user with phone payload.getPhone()?
            Optional<UserEntity> userOptional = userRepository.findByPhone(payload.getPhone());
            boolean isNewUser = true;
            if (userOptional.isPresent()) {
                isNewUser = !this.checkUserExistRegardlessOfRole(userOptional.get());
                userVerification.setUser(userOptional.get());
            }
            userVerification.setVerifiedTime(OffsetDateTime.now());
            userVerification.setStatus(VerificationStatus.VERIFIED);
            userVerification.setResendCounter(0);
            userVerification = userVerificationRepository.save(userVerification);
            return VerificationResponse.builder()
                    .userVerification(userVerification)
                    .verified(userVerification.getStatus().equals(VerificationStatus.VERIFIED))
                    .newUser(isNewUser)
                    .build();
        }
    }
    @Override
    public boolean checkUserExistRegardlessOfRole(UserEntity user){
        if(user.getRole().equals(Role.CUSTOMER)){
            return customerRepository.findByUser(user).isPresent();
        } else if(user.getRole().equals(Role.VENDOR)){
            return vendorRepository.findByUser(user).isPresent();
        }
        return user.getRole().equals(Role.SUPERADMIN);
    }

    @Override
    public Session login(VerifyOtpPayload payload, UserVerification userVerification) throws TabaldiGenericException {

        Optional<UserEntity> userOptional = userRepository.findByPhone(payload.getPhone());
        UserEntity user;
        if(!userOptional.isPresent()){
            if(payload.isAllowRegistration()){
                user = UserEntity.builder()
                        .phone(payload.getPhone())
                        .role(Role.CUSTOMER)
                        .build();
                user = userRepository.saveAndFlush(user);
            } else {
                userVerification.setStatus(VerificationStatus.EXPIRED);
                userVerification.setVerifiedTime(null);
                userVerificationRepository.save(userVerification);
                String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"User", "المستخدم");
                throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
            }
        } else {
            user = userOptional.get();
        }
        if(userVerification!=null){
            userVerification.setUser(user);
            userVerificationRepository.save(userVerification);
        }
//        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                user, null, user.getAuthorities()
//        ));
        // Generate session token for that user
        var jwtToken = jwtService.generateToken(user);
        // create session for that user to save session token
        SessionPayload sessionPayload = SessionPayload.builder()
                .token(jwtToken)
                .user(user)
                .build();
        Session loginSession;
        Optional<Session> sessionOptional = sessionRepository.findByUser(user);
        if(sessionOptional.isPresent()) {
            loginSession = sessionService.updateLoginSession(sessionPayload, sessionOptional.get());
        } else {
            loginSession = sessionService.createSession(sessionPayload);
        }
        return loginSession;
    }

    @Override
    public Session logout() throws TabaldiGenericException {
        UserEntity myUserDetails = (UserEntity) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Session session = sessionService.getSessionByUsername(myUserDetails.getUsername());

        Session logoutSession = sessionService.updateLogoutSession(session);

        return logoutSession;
    }

    @Override
    public Session refreshSessionToken(String refreshToken) throws TabaldiGenericException {

        Session session = sessionService.getSessionByRefreshToken(refreshToken);
        return sessionService.refreshSessionToken(session);
    }

    @Override
    public UserEntity getProfile(Long vendorUserId) throws TabaldiGenericException {
        UserEntity myUserDetails = (UserEntity) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Session session = sessionService.getSessionByUsername(myUserDetails.getUsername());
        UserEntity user = session.getUser();
        if(user.getRole().equals(Role.SUPERADMIN) && vendorUserId!=null){
            user = getUserById(vendorUserId);
        }
        return user;
    }

    @Override
    public UserEntity getUserById(Long userId) throws TabaldiGenericException {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"User", "المستخدم");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return userOptional.get();
    }

    @Override
    public Boolean deleteUserById(Long userId) throws TabaldiGenericException {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"User", "المستخدم");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            userRepository.deleteById(userOptional.get().getUserId());
            return true;
        }
    }
}
