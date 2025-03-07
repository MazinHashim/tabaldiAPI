package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.FileDataObject;
import com.tabaldi.api.payload.ProductPayload;
import com.tabaldi.api.repository.*;
import com.tabaldi.api.service.CategoryService;
import com.tabaldi.api.service.FileStorageService;
import com.tabaldi.api.service.ProductService;
import com.tabaldi.api.service.VendorService;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final CartItemRepository cartItemRepository;
    private final InvoiceSummaryRepository invoiceSummaryRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final CategoryService categoryService;
    private final VendorService vendorService;
    private final MessageSource messageSource;
    private final FileStorageService fileStorageService;
    private final TabaldiConfiguration configuration;

    @Override
    public Product saveProductInfo(ProductPayload payload, List<MultipartFile> productImages)
            throws TabaldiGenericException, IOException {
        // update product constraints
        List<String> imagesPaths = new ArrayList<>();
        if (payload.getProductId() != null) {
            Product product = this.getProductById(payload.getProductId());
            if (product.getVendor() != null &&
                    product.getVendor().getVendorId() != payload.getVendorId()) {
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource, "Product",
                        "المنتج");
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
            } else {
                product.setImages(GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class));
                imagesPaths = product.getImages();
            }
        }
        Category category = categoryService.getCategoryById(payload.getCategoryId());
        Vendor vendor = vendorService.getVendorById(payload.getVendorId());
        if (payload.getProductId() == null && checkIfProductExistsPerVendor(vendor, payload.getName())) {
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource, "Product", "المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        }
        if (!productImages.get(0).isEmpty() && !imagesPaths.isEmpty()) {
            fileStorageService.remove(imagesPaths.stream()
                    .map(img -> new String(Base64.getDecoder().decode(img.getBytes())))
                    .collect(Collectors.toList()));
            imagesPaths.clear();
        }
        List<FileDataObject> addList = new ArrayList<FileDataObject>();
        if (!productImages.get(0).isEmpty()) {
            for (MultipartFile image : productImages) {
                if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(image.getContentType())) {
                    String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null,
                            LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
                }
                String imagePath = configuration.getHostProductImageFolder()
                        .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                        .concat(image.getOriginalFilename()
                                .substring(image.getOriginalFilename().indexOf(".")));
                imagesPaths.add(imagePath);
                addList.add(new FileDataObject(image, imagePath));
            }
            Boolean saved = fileStorageService.save(addList);
            if (!saved) {
                String imageNotUploadedMessage = messageSource.getMessage("error.not.uploaded.file", null,
                        LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotUploadedMessage);
            }
            imagesPaths = imagesPaths.stream()
                    .map(s -> Base64.getEncoder().encodeToString(s.getBytes()))
                    .collect(Collectors.toList());
        }
        Product productParams = Product.builder()
                .name(payload.getName())
                .arName(payload.getArName())
                .duration(payload.getDuration())
                .price(payload.getPrice())
                .quantity(payload.getQuantity())
                .companyProfit(payload.getCompanyProfit())
                .imagesCollection(GenericMapper.objectToJSONMapper(imagesPaths))
                .images(imagesPaths)
                .isPublished(false)
                .category(category)
                .vendor(vendor)
                .build();
        if (payload.getProductId() != null) {
            productParams.setProductId(payload.getProductId());
        }
        if (payload.getDescription() != null)
            productParams.setDescription(payload.getDescription());
        if (payload.getArDescription() != null)
            productParams.setArDescription(payload.getArDescription());
        Product createdProduct = productRepository.save(productParams);
        createdProduct.setImages(productParams.getImages());
        if (payload.getProductId() == null && (payload.getOptions() != null && !payload.getOptions().isEmpty())) {
            Product finalCreatedProduct = createdProduct;
            payload.getOptions().forEach(option -> {
                option.setProduct(finalCreatedProduct);
            });
            createdProduct.setOptions(optionRepository.saveAll(payload.getOptions()));
        }
        return createdProduct;
    }

    @Override
    public Boolean togglePublishedById(Long productId) throws TabaldiGenericException, IOException {
        Product product = this.getProductById(productId);
        int updated = productRepository.togglePublishedById(!product.isPublished(), product.getProductId());
        if (updated > 0)
            return !product.isPublished();
        return product.isPublished();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProductById(Long productId) throws TabaldiGenericException, IOException {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Product", "المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Product product = productOptional.get();
            List<String> imagesPaths = GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class);
            List<String> decodedImagesPaths = imagesPaths.stream().map(imgPath -> new String(Base64.getDecoder().decode(imgPath))).collect(Collectors.toList());
            Boolean removed = fileStorageService.remove(decodedImagesPaths);
            if (!removed) {
                String imageNotRemovedMessage = messageSource.getMessage("error.not.removed.file", null,
                        LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, imageNotRemovedMessage);
            }
            cartItemRepository.deleteByProduct_productId(product.getProductId());
            invoiceRepository.deleteByOrder_cartItems_product_productId(product.getProductId());
            orderRepository.deleteByCartItems_product_productId(product.getProductId());
            optionRepository.deleteByProduct_productId(product.getProductId());
            productRepository.deleteById(product.getProductId());
            return true;
        }
    }

    @Override
    public Product getProductById(Long productId) throws TabaldiGenericException, IOException {
        Optional<Product> selectedProduct = productRepository.findById(productId);
        if (!selectedProduct.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Product", "المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        Product product = selectedProduct.get();
        product.setImages(GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class));
        return product;
    }

    @Override
    public List<Product> searchProductByQuery(String query) throws TabaldiGenericException, IOException {
        List<Product> products = productRepository.findByNameOrArNameContainingIgnoreCase(query);
        if (products.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Products", "المنتجات");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (Product product : products) {
            product.setImages(GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class));
        }
        return products;
    }

    @Override
    public Long countByCategory(Long categoryId) {
        return productRepository.countByCategory(categoryId);
    }

    @Override
    public List<CartItem> getProductCartItemsList(Long productId) throws TabaldiGenericException, IOException {
        Product product = this.getProductById(productId);
        List<CartItem> cartItems = cartItemRepository.findByProduct(product);
        if (cartItems.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Items", "أغراض السلة");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (CartItem cartItem : cartItems) {
            if (cartItem.getOptionsCollection() != null && !cartItem.getOptionsCollection().isEmpty()) {
                cartItem.setSelectedOptions(
                        GenericMapper.jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
            }
            cartItem.getProduct().setImages(
                    GenericMapper.jsonToListObjectMapper(cartItem.getProduct().getImagesCollection(), String.class));
        }
        return cartItems;
    }

    private boolean checkIfProductExistsPerVendor(Vendor selectedVendor, String name) {
        Optional<Product> product = productRepository.findByVendorAndName(selectedVendor.getVendorId(), name);
        return product.isPresent();
    }

    @Override
    public Product addProductImage(Long productId, MultipartFile image) throws TabaldiGenericException, IOException {
        Product product = this.getProductById(productId);
        List<String> imagesPaths = GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class);

        if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(image.getContentType())) {
            String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
        }

        String imagePath = configuration.getHostProductImageFolder()
                .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                .concat(image.getOriginalFilename()
                        .substring(image.getOriginalFilename().indexOf(".")));

        FileDataObject fileDataObject = new FileDataObject(image, imagePath);
        Boolean saved = fileStorageService.save(Collections.singletonList(fileDataObject));

        if (!saved) {
            String imageNotUploadedMessage = messageSource.getMessage("error.not.uploaded.file", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotUploadedMessage);
        }

        String encodedImagePath = Base64.getEncoder().encodeToString(imagePath.getBytes());
        imagesPaths.add(encodedImagePath);

        product.setImagesCollection(GenericMapper.objectToJSONMapper(imagesPaths));
        product.setImages(imagesPaths);

        return productRepository.save(product);
    }

    @Override
    public Product removeProductImage(Long productId, String imagePathToRemove)
            throws TabaldiGenericException, IOException {
        Product product = this.getProductById(productId);
        List<String> imagesPaths = GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class);

        String decodedImagePath = new String(Base64.getDecoder().decode(imagePathToRemove));
        if(imagesPaths.size()==1){
            String imageNotRemovedMessage = messageSource.getMessage("error.not.removed.file", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotRemovedMessage);
        }
        if (!imagesPaths.remove(imagePathToRemove)) {
            String imageNotFoundMessage = messageSource.getMessage("error.image.not.found", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, imageNotFoundMessage);
        }

        Boolean removed = fileStorageService.remove(Collections.singletonList(decodedImagePath));
        if (!removed) {
            String imageNotRemovedMessage = messageSource.getMessage("error.not.removed.file", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, imageNotRemovedMessage);
        }

        product.setImagesCollection(GenericMapper.objectToJSONMapper(imagesPaths));
        product.setImages(imagesPaths);

        return productRepository.save(product);
    }
}
