package com.tabaldi.api.response;

import com.tabaldi.api.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemListResponse extends GenericResponse {
    private String event;
    private List<CartItem> cartItems;
}
