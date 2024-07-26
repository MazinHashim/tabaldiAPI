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
    List<CartItem> updateQuantityById(Long cartItemId, int newQuantity) throws TabaldiGenericException, IOException;
    List<CartItem> saveCartItemInfo(CartItemPayload payload) throws TabaldiGenericException, IOException;
    List<CartItem> deleteCartItemById(Long cartItemId) throws TabaldiGenericException, IOException;
}
