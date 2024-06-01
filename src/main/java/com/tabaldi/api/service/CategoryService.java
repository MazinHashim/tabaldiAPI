package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Category;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.payload.CategoryPayload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface CategoryService {

    Category getCategoryById(Long categoryId) throws TabaldiGenericException;
    Category saveCategoryInfo(CategoryPayload payload) throws TabaldiGenericException;
    Boolean deleteCategoryById(Long categoryId) throws TabaldiGenericException;
    Boolean togglePublishedById(Long categoryId) throws TabaldiGenericException;
    List<Product> getCategoryProductsList(Long categoryId) throws TabaldiGenericException, IOException;
}
