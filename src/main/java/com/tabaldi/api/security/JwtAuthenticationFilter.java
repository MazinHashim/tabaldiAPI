package com.tabaldi.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // use this for postman
        final String authHeader = request.getHeader("Authorization");
//        final String authHeader = request.getHeader("Authentication");
        final String jwt;
        final String userPhone;
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        try {
            // TODO:: Extract the user phone from JWT token;
            userPhone = jwtService.extractUsername(jwt);

            // TODO:: Check is the user is already authenticated
            if (userPhone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // TODO:: Check if the user existing in the database and return his username(phone)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userPhone);
                // TODO:: Check is the token's user is the same as db's user and his token is not expired
                if(jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // TODO:: if token is valid then update security context holder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // Catch for UsernameNotFoundException, ExpiredJwtException
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        filterChain.doFilter(request, response);
    }
}
