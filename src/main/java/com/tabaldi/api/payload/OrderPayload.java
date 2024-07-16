package com.tabaldi.api.payload;

import com.tabaldi.api.model.PaymentMethod;
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
public class OrderPayload {

    private Double discount;
    private double shippingCost;
    private int taxPercentage;
    private String comment;
    @NotNull
    private PaymentMethod paymentMethod;
}
