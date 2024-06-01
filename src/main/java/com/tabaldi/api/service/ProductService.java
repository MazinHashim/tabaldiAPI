package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Category;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.payload.ProductPayload;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface ProductService {

    Product saveProductInfo(ProductPayload payload, List<MultipartFile> productImages) throws TabaldiGenericException, IOException;

    Boolean deleteProductById(Long productId) throws TabaldiGenericException;
    Long countByCategory(Long categoryId);
    Product getProductById(Long productId) throws TabaldiGenericException, IOException;
    List<CartItem> getProductCartItemsList(Long productId) throws TabaldiGenericException, IOException;
}
