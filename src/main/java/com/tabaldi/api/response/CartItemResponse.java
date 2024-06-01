package com.tabaldi.api.response;

import com.tabaldi.api.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse extends GenericResponse {
    private String event;
    private CartItem cartItem;
}
