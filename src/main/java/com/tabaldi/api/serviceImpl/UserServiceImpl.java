package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.SendOtpPayload;
import com.tabaldi.api.payload.SessionPayload;
import com.tabaldi.api.payload.UserPayload;
import com.tabaldi.api.payload.VerifyOtpPayload;
import com.tabaldi.api.repository.*;
import com.tabaldi.api.response.VerificationResponse;
import com.tabaldi.api.security.JwtService;
import com.tabaldi.api.service.SessionService;
import com.tabaldi.api.service.SmsService;
import com.tabaldi.api.service.UserService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.List;
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
    private final SmsService smsService;
    private final SessionService sessionService;
    private final TabaldiConfiguration configuration;
    @Value("${spring.profiles.active}")
    private String profile;

    @Override
    public List<UserEntity> getAdminUsersList() throws TabaldiGenericException {
        List<UserEntity> userList = userRepository.findByVendorIsNullAndRole(Role.SUPERADMIN);

        if(userList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Users", "المستخدمين");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return userList;
    }

    @Override
    public UserEntity addUser(UserPayload payload) throws TabaldiGenericException {
        UserEntity user=null;
        if(payload.getUserId()!=null) {
            user = this.getUserById(payload.getUserId());
        }
        UserEntity existEmail = this.getExistByEmail(payload.getEmail());
        UserEntity existPhone = this.getExistByPhone(payload.getPhone());

        if(existEmail==null && existPhone==null){
            user = UserEntity.builder()
                    .phone(payload.getPhone())
                    .email(payload.getEmail())
                    .agreeTermsConditions(payload.isAgreeTermsConditions())
                    .role(Role.SUPERADMIN)
                    .isSuperAdmin(false)
                    .build();
            if(payload.getUserId()!=null){
                user.setUserId(payload.getUserId());
            }
        } else if(user!=null && existPhone == null || existPhone.getUserId()==user.getUserId()) {
            user.setPhone(payload.getPhone());
        } else if(user!=null && existEmail == null || existEmail.getUserId()==user.getUserId()){
            user.setEmail(payload.getEmail());
//        } else {
//            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"phone", "رقم الهاتف");
//            if((user == null && existEmail!=null) || (user != null && existEmail.getUserId()!=user.getUserId())){
//                alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"email", "البريد الإلكتروني");
//            }
//            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        }

        if((user == null && existEmail!=null) || (user != null && existEmail!=null && existEmail.getUserId()!=user.getUserId())){
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"email", "البريد الإلكتروني");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        } else if ((user == null && existPhone!=null) || (user != null && existPhone!=null && existPhone.getUserId()!=user.getUserId())) {
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"phone", "رقم الهاتف");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        }
        user = userRepository.saveAndFlush(user);
        return user;
    }

    @Override
    public UserVerification sendOtp(SendOtpPayload payload)
            throws TabaldiGenericException, UnsupportedEncodingException {
        Pattern pattern = Pattern.compile("05+\\d{8}");
        boolean matches;
        Matcher mat = pattern.matcher(payload.getPhone());
        matches = mat.matches();
        if (!matches) {
            String phoneInvalidMessage = MessagesUtils.getInvalidFormatMessage(messageSource,
                    payload.getPhone(), payload.getPhone(), "phone", "رقم الهاتف");

            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, phoneInvalidMessage);
        }
        Optional<UserEntity> userOptional = userRepository.findByPhone(payload.getPhone());
        if (!userOptional.isPresent() && payload.isCheckExistence()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "User", "المستخدم");

            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Random random = new Random();
            int otpCode = random.ints(1111, 9999).findFirst().getAsInt();

            // get last active otp which will expire after expireDuration
            Optional<UserVerification> userVerification = userVerificationRepository
                    .findLastSentCode(payload.getPhone());
            int resendTimes = 0;
            if (userVerification.isPresent()) {
                resendTimes = userVerification.get().getResendCounter();
                if (userVerification.get().getCreatedTime().isBefore(OffsetDateTime.now().minusHours(24))) {
                    resendTimes = 0;
                }
                if (resendTimes >= Integer.parseInt(configuration.getOtpResendTimesLimit())) {
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
                            messageSource.getMessage("error.exceed.resend.limit", null,
                                    LocaleContextHolder.getLocale()));
                }
                if (userVerification.get().getExpiryTime().isAfter(OffsetDateTime.now())) {
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
                            messageSource.getMessage("error.code.not.expired", null,
                                    LocaleContextHolder.getLocale()));
                }
            }

            String uuid = RandomString.make(64);
            UserVerification verification = UserVerification.builder()
                    .code(1111)
                    .phone(payload.getPhone())
                    .resendCounter(resendTimes + 1)
                    .createdTime(OffsetDateTime.now())
                    .expiryTime(OffsetDateTime.now().plusMinutes(Integer.parseInt(configuration.getOtpExpirationMin())))
                    .status(VerificationStatus.SENT)
                    .user(userOptional.isPresent() ? userOptional.get() : null)
                    .verifiedTime(null)
                    .keyRef(uuid)
                    .build();

            // send otp sms using smart sms gateway
            if (!profile.equals("local") || !profile.equals("dev")) {
                // String response = smsService.sendSms(payload.getPhone(), "Welcome your
                // Rateena sign in OTP code is " + otpCode);
                // System.out.println(response);
                // if (!response.contains("OK")) {
                // throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
                // messageSource.getMessage("error.exceed.resend.limit", null,
                // LocaleContextHolder.getLocale()) + "Error " + response);
                // }
            }

            return userVerificationRepository.saveAndFlush(verification);
        }
    }

    @Override
    public VerificationResponse verifyOtp(VerifyOtpPayload payload) throws TabaldiGenericException {

        UserVerification userVerification = userVerificationRepository
                .findByPhoneAndCodeAndKeyRef(payload.getPhone(), payload.getOtpCode(),
                        payload.getKeyRef())
                .orElseThrow(() -> {
                    String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Otp", "كود التحقق");

                    return new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
                });
        if (OffsetDateTime.now().isAfter(userVerification.getExpiryTime())
                || userVerification.getVerifiedTime() != null
                || userVerification.getStatus().equals(VerificationStatus.EXPIRED)) {
            userVerification.setStatus(VerificationStatus.EXPIRED);
            userVerificationRepository.save(userVerification);
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
                    messageSource.getMessage("error.verification.code.expired", null,
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
    public boolean checkUserExistRegardlessOfRole(UserEntity user) {
        if (user.getRole().equals(Role.CUSTOMER)) {
            return customerRepository.findByUser(user).isPresent();
        } else if (user.getRole().equals(Role.VENDOR)) {

            return userRepository.findById(user.getUserId()).isPresent();
        }
        return user.getRole().equals(Role.SUPERADMIN);
    }

    @Override
    public UserEntity changeUserPhoneNumber(long userId, String newPhoneNumber, VerifyOtpPayload payload)
            throws TabaldiGenericException {
        UserEntity myUserDetails = (UserEntity) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Session session = sessionService.getSessionByUsername(myUserDetails.getUsername());
        UserEntity authUser = session.getUser();

        UserEntity user = this.getUserById(userId);
        if (userRepository.findByPhone(newPhoneNumber).isPresent()) {
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource, "User", "المستخدم");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        }

        if (authUser.getRole().equals(Role.CUSTOMER) && authUser.getPhone().equals(user.getPhone())) {
            VerificationResponse response = this.verifyOtp(payload);
            if (response.isVerified()) {
                user.setPhone(newPhoneNumber);
                userRepository.save(user);
            }
        } else {
            String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource, "phone number",
                    "الرقم");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
        }
        return user;
    }

    @Override
    public UserEntity getExistByEmail(String email) {
        Optional<UserEntity> userEntityE = userRepository.findByEmail(email);
        if(userEntityE.isPresent()) return userEntityE.get();
        return null;
    }
    @Override
    public UserEntity getExistByPhone(String phone) {
        Optional<UserEntity> userEntityP = userRepository.findByPhone(phone);
        if(userEntityP.isPresent()) return userEntityP.get();
        return null;
    }

    @Override
    public Session login(VerifyOtpPayload payload, UserVerification userVerification) throws TabaldiGenericException {

        Optional<UserEntity> userOptional = userRepository.findByPhone(payload.getPhone());
        UserEntity user;
        if (!userOptional.isPresent()) {
            if (payload.isAllowRegistration()) {
                user = UserEntity.builder()
                        .phone(payload.getPhone())
                        .role(Role.CUSTOMER)
                        .build();
                user = userRepository.saveAndFlush(user);
            } else {
                userVerification.setStatus(VerificationStatus.EXPIRED);
                userVerification.setVerifiedTime(null);
                userVerificationRepository.save(userVerification);
                String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "User", "المستخدم");
                throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
            }
        } else {
            user = userOptional.get();
        }
        if (user.getRole().equals(Role.CUSTOMER)
                && (payload.getDeviceToken() == null || payload.getDeviceToken().isEmpty())) {
            String deviceTokenRequiredMessage = messageSource.getMessage("error.device.token.required", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, deviceTokenRequiredMessage);
        }
        if (userVerification != null) {
            userVerification.setUser(user);
            userVerificationRepository.save(userVerification);
        }
        // authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
        // user, null, user.getAuthorities()
        // ));
        // Generate session token for that user
        var jwtToken = jwtService.generateToken(user);
        // create session for that user to save session token
        SessionPayload sessionPayload = SessionPayload.builder()
                .token(jwtToken)
                .deviceToken(payload.getDeviceToken())
                .user(user)
                .build();
        Session loginSession;
        Optional<Session> sessionOptional = sessionRepository.findByUser(user);
        if (sessionOptional.isPresent()) {
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
        if (user.getRole().equals(Role.SUPERADMIN) && vendorUserId != null) {
            user = getUserById(vendorUserId);
        }
        if(user.getVendor()!=null){
            user.getVendor().setUserId(user.getUserId());
            user.getVendor().setUserEmail(user.getEmail());
            user.getVendor().setUserPhone(user.getPhone());
        }
        return user;
    }

    @Override
    public UserEntity getUserById(Long userId) throws TabaldiGenericException {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "User", "المستخدم");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return userOptional.get();
    }

    @Override
    public Boolean deleteUserById(Long userId) throws TabaldiGenericException {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "User", "المستخدم");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            userRepository.deleteById(userOptional.get().getUserId());
            return true;
        }
    }
}
