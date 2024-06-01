package com.tabaldi.api.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPayload {

    private @NotNull @NotEmpty String phone;
    private @NotNull @NotEmpty String email;
    private boolean agreeTermsConditions = false;
    private @NotNull @NotEmpty String keyRef;
    private Integer otpCode;
}
