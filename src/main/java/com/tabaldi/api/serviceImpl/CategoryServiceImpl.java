package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.CategoryPayload;
import com.tabaldi.api.repository.CategoryRepository;
import com.tabaldi.api.repository.ProductRepository;
import com.tabaldi.api.service.*;
import com.tabaldi.api.service.VendorService;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final VendorService vendorService;
    private final ProductRepository productRepository;
    private final MessageSource messageSource;

    @Override
    public Category getCategoryById(Long categoryId) throws TabaldiGenericException {
        Optional<Category> selectedCategory = categoryRepository.findById(categoryId);
        if (!selectedCategory.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Category", "نوع المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedCategory.get();
    }

    @Override
    public Category saveCategoryInfo(CategoryPayload payload) throws TabaldiGenericException {
        // update category constraints
        // NOTE it should not be updatable but kept for any changes in future that makes
        // it updatable
        boolean isUpdatedCategoryPublished = false;
        if (payload.getCategoryId() != null) {
            Optional<Category> categoryOptional = categoryRepository.findById(payload.getCategoryId());
            if (!categoryOptional.isPresent()) {
                String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Category", "العنوان");
                throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
            } else if (categoryOptional.get().getVendor() != null &&
                    categoryOptional.get().getVendor().getVendorId() != payload.getVendorId()) {
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource, "Category",
                        "نوع المنتج");
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
                // } else if(payload.getName()!=categoryOptional.get().getName()){
                // String categoryUpdateMessage =
                // messageSource.getMessage("error.category.update", null,
                // LocaleContextHolder.getLocale());
                // throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
                // categoryUpdateMessage);
            } else
                isUpdatedCategoryPublished = categoryOptional.get().isPublished();
        }
        Vendor selectedVendor = vendorService.getVendorById(payload.getVendorId());
        if (payload.getCategoryId() == null && this.checkIfNameExistsPerVendor(selectedVendor, payload.getName())) {
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource, "Category", "نوع المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, "إسم " + alreadyExistMessage);
        } else {
            Category categoryParams = Category.builder()
                    .name(payload.getName())
                    .arName(payload.getArName())
                    .isPublished(payload.getCategoryId() != null
                            ? isUpdatedCategoryPublished
                            : payload.isPublished())
                    .vendor(selectedVendor)
                    .build();
            if (payload.getCategoryId() != null) {
                categoryParams.setCategoryId(payload.getCategoryId());
            }
            return categoryRepository.save(categoryParams);
        }
    }

    @Override
    public Boolean deleteCategoryById(Long categoryId) throws TabaldiGenericException {
        Category category = this.getCategoryById(categoryId);
        if (productRepository.countByCategory(categoryId) != 0) {
            String categoryUpdateMessage = messageSource.getMessage("error.category.has.product", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, categoryUpdateMessage);
        } else {
            categoryRepository.deleteById(category.getCategoryId());
            return true;
        }
    }

    @Override
    public Boolean togglePublishedById(Long categoryId) throws TabaldiGenericException {
        Category category = this.getCategoryById(categoryId);
        int updated = categoryRepository.togglePublishedById(!category.isPublished(), category.getCategoryId());
        if (updated > 0)
            return !category.isPublished();
        return category.isPublished();
    }

    @Override
    public List<Product> getCategoryProductsList(Long categoryId) throws TabaldiGenericException, IOException {
        Category category = this.getCategoryById(categoryId);
        List<Product> products = productRepository.findByCategory(category);
        if (products.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Product", "المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (Product product : products) {
            product.setImages(GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class));
        }
        return products;
    }

    private boolean checkIfNameExistsPerVendor(Vendor selectedVendor, String name) {
        Optional<Category> category = categoryRepository.findByVendorAndName(selectedVendor.getVendorId(), name);
        return category.isPresent();
    }
}
