package com.tabaldi.api.response;

import com.tabaldi.api.model.UserVerification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationResponse extends GenericResponse {
    private boolean verified;
    private boolean newUser;
    private UserVerification userVerification;
}
