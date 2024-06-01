package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Session;
import com.tabaldi.api.model.UserEntity;
import com.tabaldi.api.payload.SessionPayload;
import com.tabaldi.api.repository.SessionRepository;
import com.tabaldi.api.repository.UserRepository;
import com.tabaldi.api.security.JwtService;
import com.tabaldi.api.service.SessionService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class SessionServiceImpl implements SessionService {
    @Autowired
    private SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    private final JwtService jwtService;
    public Session createSession(@Valid SessionPayload payload) {
        Session session = Session.builder()
                .sessionToken(payload.getToken())
                .refreshToken(RandomString.make(40))
                .user(payload.getUser())
                .lastLogin(LocalDateTime.now())
                .build();
        session = sessionRepository.save(session);
        return session;
    }

    public Session updateLoginSession(SessionPayload payload, Session loginSession) throws TabaldiGenericException {

        loginSession.setLastLogin(LocalDateTime.now());
        loginSession.setSessionToken(payload.getToken());
        loginSession = sessionRepository.save(loginSession);

        return loginSession;
    }

    public Session updateLogoutSession(@Valid Session session) {

        session.setLastLogout(LocalDateTime.now());
        session.setSessionToken(null);
        session = sessionRepository.save(session);

        return session;
    }

    public Session getSessionByUsername(String username) throws TabaldiGenericException {
        UserEntity user = userRepository.findByPhone(username).orElseThrow(()->{
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"User", "المستخدم");

            return new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        });
        Session session = sessionRepository.findByUser(user).orElseThrow(()->{
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Session", "السيشن");

            return new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        });
        return session;
    }

    public Session getSessionByRefreshToken(String refreshToken) throws TabaldiGenericException{
        Session session = sessionRepository.findByRefreshToken(refreshToken).orElseThrow(()->{
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Session", "السيشن");

            return new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        });
        return session;
    }

    public Session refreshSessionToken(Session session) {

        String sessionToken = jwtService.generateToken(session.getUser());
        session.setRefreshToken(RandomString.make(40));
        session.setSessionToken(sessionToken);
        Session refreshSession = sessionRepository.save(session);

        return refreshSession;
    }
}
