package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.payload.ProductPayload;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface ProductService {

    Product saveProductInfo(ProductPayload payload, List<MultipartFile> productImages)
            throws TabaldiGenericException, IOException;

    Boolean deleteProductById(Long productId) throws TabaldiGenericException, IOException;

    List<Product> searchProductByQuery(String query) throws TabaldiGenericException, IOException;

    Long countByCategory(Long categoryId);

    Boolean togglePublishedById(Long productId) throws TabaldiGenericException, IOException;

    Product getProductById(Long productId) throws TabaldiGenericException, IOException;

    List<CartItem> getProductCartItemsList(Long productId) throws TabaldiGenericException, IOException;

    Product addProductImage(Long productId, MultipartFile image) throws TabaldiGenericException, IOException;

    Product removeProductImage(Long productId, String imagePathToRemove) throws TabaldiGenericException, IOException;
}
