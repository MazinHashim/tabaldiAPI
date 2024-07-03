package com.tabaldi.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse extends GenericResponse{
    private String token;
    private String refreshToken;
    private String role;    // this for super admin and vendor for redirect to authorized route
    private boolean newUser;  // this for customer to add customer info
    private long userId;  // this for customer to pass it for adding customer info
}