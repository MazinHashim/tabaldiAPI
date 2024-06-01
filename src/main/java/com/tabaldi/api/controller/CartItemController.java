package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.payload.CartItemPayload;
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

    @PostMapping("/save")
    public @ResponseBody ResponseEntity<CartItemResponse> saveCartItem (
            @RequestBody @Valid CartItemPayload payload) throws TabaldiGenericException, IOException {

        CartItem cartItem = cartItemService.saveCartItemInfo(payload);
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "CartItem", "عنصر السلة", "created", "حفظ");
        return ResponseEntity.ok(
                CartItemResponse.builder()
                        .event("created")
                        .cartItem(cartItem)
                        .message(successSaveMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{cartItemId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteCartItem (@PathVariable("cartItemId") Long cartItemId)
            throws TabaldiGenericException {
        Boolean isDeleted = cartItemService.deleteCartItemById(cartItemId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "CartItem", "عنصر السلة");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }
}
