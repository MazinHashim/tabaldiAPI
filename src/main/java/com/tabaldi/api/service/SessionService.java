package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Session;
import com.tabaldi.api.payload.SessionPayload;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface SessionService {
    Session createSession(@Valid SessionPayload payload);

    Session updateLoginSession(SessionPayload payload, Session loginSession) throws TabaldiGenericException;

    Session updateLogoutSession(@Valid Session session);

    Session getSessionByUsername(String username) throws TabaldiGenericException;

    Session getSessionByRefreshToken(String refreshToken) throws TabaldiGenericException;

    Session refreshSessionToken(Session session);

}
