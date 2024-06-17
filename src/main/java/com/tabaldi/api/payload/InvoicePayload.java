package com.tabaldi.api.payload;

import com.tabaldi.api.model.InvoiceStatus;
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
public class InvoicePayload {

    // invoice number and issueDate is generated in backend
    @NotNull
    @NotEmpty
    private String paymentMethod;
    @NotNull
    private double subtotal;
    @NotNull
    private double discount;
    @NotNull
    private double taxes;
    @NotNull
    private double shippingCost;
    @NotNull
    private double total;
    @NotNull
    private Long orderId;
}
