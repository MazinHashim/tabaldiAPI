package com.tabaldi.api.payload;

import com.tabaldi.api.model.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPayload {

    private Double discount;
    private int taxPercentage;
    private String comment;
    @NotNull
    private PaymentMethod paymentMethod;
    private CardPayload card;
    @NotNull
    @NotEmpty
    private List<ShippingCostPayload> shippingCosts;
    private String token;
}
