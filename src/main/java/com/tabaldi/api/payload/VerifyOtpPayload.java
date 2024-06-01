package com.tabaldi.api.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpPayload {

    @NotNull @NotEmpty private String phone;
    @NotNull @NotEmpty private String keyRef;
    private boolean allowRegistration = false;
    private Integer otpCode;
}
