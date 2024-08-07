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
public class DirectPaymentPayload {
    @NotNull
    private Boolean SaveToken;
    @NotNull
    @NotEmpty
    private String PaymentType;
    private CardPayload Card;
    @NotNull
    private Boolean Bypass3DS;
    private String Token;
}