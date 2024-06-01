package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.payload.ProductPayload;
import com.tabaldi.api.response.ListResponse;
import com.tabaldi.api.response.ProductResponse;
import com.tabaldi.api.response.DeleteResponse;
import com.tabaldi.api.service.ProductService;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final MessageSource messageSource;

    @GetMapping("/{productId}")
    public @ResponseBody ResponseEntity<ProductResponse> getById (@PathVariable("productId") Long productId)
            throws TabaldiGenericException, IOException {
        Product product = productService.getProductById(productId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Product", "المنتج");

        return ResponseEntity.ok(ProductResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .product(product).build());

    }

    @PostMapping("/save")
    public @ResponseBody ResponseEntity<ProductResponse> saveProduct (
            @Valid @RequestParam("productPayload") final String payload,
            @Valid @RequestParam("productImages") final List<MultipartFile> productImages) throws TabaldiGenericException, IOException {

        ProductPayload productPayload = GenericMapper.jsonToObjectMapper(payload, ProductPayload.class);
        Product product = productService.saveProductInfo(productPayload, productImages);
        String event = productPayload.getProductId()==null?"created":"updated";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "Product", "المنتج", event, event.equals("created")?"حفظ":"تعديل");
        return ResponseEntity.ok(
                ProductResponse.builder()
                        .event(event)
                        .product(product)
                        .message(successSaveMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{productId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteProduct (@PathVariable("productId") Long productId)
            throws TabaldiGenericException {
        Boolean isDeleted = productService.deleteProductById(productId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Product", "المنتج");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }

    @GetMapping("/{productId}/cart-items")
    public @ResponseBody ResponseEntity<ListResponse<CartItem>> getProductCartItemsList (
            @PathVariable("productId") Long productId) throws TabaldiGenericException, IOException {
        List<CartItem> cartItemsList = productService.getProductCartItemsList(productId);
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Cart Items", "أغراض السلة");
        return ResponseEntity.ok(
                ListResponse.<CartItem>genericBuilder()
                        .list(cartItemsList)
                        .message(fetchMessage)
                        .build()
        );
    }
}
