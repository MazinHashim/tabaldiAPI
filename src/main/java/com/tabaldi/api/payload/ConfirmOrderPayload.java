package com.tabaldi.api.payload;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmOrderPayload {

    @NotNull
    private Long orderId;
    @NotNull
    private Long customerId;
}
