package com.tabaldi.api.payload;

import com.tabaldi.api.model.Role;
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
    private Long vendorId;
    private Long userId;
    @Builder.Default
    private Role role = Role.VENDOR;
    @Builder.Default
    private boolean agreeTermsConditions = false;
}
