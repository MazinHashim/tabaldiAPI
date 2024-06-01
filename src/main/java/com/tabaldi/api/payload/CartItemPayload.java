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
public class CartItemPayload {

//    no update
//    private Long cartItemId;
    @NotNull
    private int quantity;
    @NotNull
    private double price;
    private String comment;
    private String options;

    @NotNull
    private Long productId;
    @NotNull
    private Long customerId;

}
