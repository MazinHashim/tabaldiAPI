package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Category;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.payload.CategoryPayload;
import com.tabaldi.api.response.PublishResponse;
import com.tabaldi.api.response.CategoryResponse;
import com.tabaldi.api.response.DeleteResponse;
import com.tabaldi.api.response.ListResponse;
import com.tabaldi.api.service.CategoryService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageSource messageSource;

    @GetMapping("/{categoryId}")
    public @ResponseBody ResponseEntity<CategoryResponse> getById (@PathVariable("categoryId") Long categoryId)
            throws TabaldiGenericException {
        Category category = categoryService.getCategoryById(categoryId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Category", "نوع المنتج");

        return ResponseEntity.ok(CategoryResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .category(category).build());

    }

    @PostMapping("/save")
    public @ResponseBody ResponseEntity<CategoryResponse> saveCategory (
            @RequestBody @Valid CategoryPayload payload) throws TabaldiGenericException {

        Category category = categoryService.saveCategoryInfo(payload);
        String event = payload.getCategoryId()==null?"created":"updated";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "Category", "نوع المنتج", event, event.equals("created")?"حفظ":"تعديل");
        return ResponseEntity.ok(
                CategoryResponse.builder()
                        .event(event)
                        .category(category)
                        .message(successSaveMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{categoryId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteCategory (@PathVariable("categoryId") Long categoryId)
            throws TabaldiGenericException {
        Boolean isDeleted = categoryService.deleteCategoryById(categoryId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Category", "العنوان");
        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }
    @GetMapping("/toggle/publish/{categoryId}")
    public @ResponseBody ResponseEntity<PublishResponse> togglePublishedCategory (@PathVariable("categoryId") Long categoryId)
            throws TabaldiGenericException {
        Boolean isPublished = categoryService.togglePublishedById(categoryId);
        String successPublishedMessage = MessagesUtils.getPublishMessage(messageSource, isPublished?"published":"unpublished", isPublished?"نشر":"إيقاف", "Category", "الصنف");

        return ResponseEntity.ok(PublishResponse.builder()
                .message(successPublishedMessage)
                .isPublished(isPublished).build());

    }

    @GetMapping("/{categoryId}/products")
    public @ResponseBody ResponseEntity<ListResponse<Product>> getProductsList (
            @PathVariable("categoryId") long categoryId) throws TabaldiGenericException, IOException {
        List<Product> productsList = categoryService.getCategoryProductsList(categoryId); // may add filters
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Products", "المنتجات");
        return ResponseEntity.ok(
                ListResponse.<Product>genericBuilder()
                        .list(productsList)
                        .message(fetchMessage)
                        .build()
        );
    }
}
