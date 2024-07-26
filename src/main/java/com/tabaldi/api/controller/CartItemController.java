package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.payload.CartItemPayload;
import com.tabaldi.api.payload.QuantityPayload;
import com.tabaldi.api.response.CartItemListResponse;
import com.tabaldi.api.response.CartItemResponse;
import com.tabaldi.api.response.DeleteResponse;
import com.tabaldi.api.service.CartItemService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cartItems")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartItemController {

    private final CartItemService cartItemService;
    private final MessageSource messageSource;

    @GetMapping("/{cartItemId}")
    public @ResponseBody ResponseEntity<CartItemResponse> getById (@PathVariable("cartItemId") Long cartItemId)
            throws TabaldiGenericException, IOException {
        CartItem cartItem = cartItemService.getCartItemById(cartItemId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "CartItem", "عنصر السلة");

        return ResponseEntity.ok(CartItemResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .cartItem(cartItem).build());

    }
    @PostMapping("/update/quantity")
    public @ResponseBody ResponseEntity<CartItemListResponse> updateQuantityById(
            @RequestBody @Valid QuantityPayload payload) throws TabaldiGenericException, IOException {
        List<CartItem> updateCarts = cartItemService.updateQuantityById(payload.getCartItemId(), payload.getNewQuantity());
        String successFetchMessage = MessagesUtils.getSavedDataMessage(messageSource, "CartItem", "عنصر السلة", "updated", "تعديل");

        return ResponseEntity.ok(CartItemListResponse.builder()
                .message(successFetchMessage)
                .event("updated")
                .cartItems(updateCarts).build());

    }

    @PostMapping("/save")
    public @ResponseBody ResponseEntity<CartItemListResponse> saveCartItem (
            @RequestBody @Valid CartItemPayload payload) throws TabaldiGenericException, IOException {

        List<CartItem> cartItems = cartItemService.saveCartItemInfo(payload);
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "CartItem", "عنصر السلة", "created", "حفظ");
        return ResponseEntity.ok(
                CartItemListResponse.builder()
                        .event("created")
                        .cartItems(cartItems)
                        .message(successSaveMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{cartItemId}")
    public @ResponseBody ResponseEntity<CartItemListResponse> deleteCartItem (@PathVariable("cartItemId") Long cartItemId)
            throws TabaldiGenericException, IOException {
        List<CartItem> afterDeleted = cartItemService.deleteCartItemById(cartItemId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "CartItem", "عنصر السلة");

        return ResponseEntity.ok(CartItemListResponse.builder()
                .event("deleted")
                .cartItems(afterDeleted)
                .message(successDeleteMessage)
                .build());

    }
}
