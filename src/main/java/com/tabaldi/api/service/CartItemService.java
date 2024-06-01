package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.payload.CartItemPayload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface CartItemService {
    CartItem getCartItemById(Long cartItemId) throws TabaldiGenericException, IOException;
    CartItem saveCartItemInfo(CartItemPayload payload) throws TabaldiGenericException, IOException;
    Boolean deleteCartItemById(Long cartItemId) throws TabaldiGenericException;
}
