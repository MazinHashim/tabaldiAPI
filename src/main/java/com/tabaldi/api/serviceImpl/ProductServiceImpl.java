package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.FileDataObject;
import com.tabaldi.api.payload.ProductPayload;
import com.tabaldi.api.repository.CartItemRepository;
import com.tabaldi.api.repository.OptionRepository;
import com.tabaldi.api.repository.ProductRepository;
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
    private final CategoryService categoryService;
    private final VendorService vendorService;
    private final MessageSource messageSource;
    private final FileStorageService fileStorageService;
    private final TabaldiConfiguration configuration;

    @Override
    public Product saveProductInfo(ProductPayload payload, List<MultipartFile> productImages) throws TabaldiGenericException, IOException {
        // update product constraints
        List<String> imagesPaths = new ArrayList<>();
        if (payload.getProductId() != null) {
            Product product = this.getProductById(payload.getProductId());
            if (product.getVendor() != null &&
                    product.getVendor().getVendorId() != payload.getVendorId()) {
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource, "Product", "المنتج");
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
        List<FileDataObject> addList = new ArrayList();
        if (!productImages.get(0).isEmpty()) {
            for (MultipartFile image : productImages) {
                if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(image.getContentType())) {
                    String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null, LocaleContextHolder.getLocale());
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
                String imageNotUploadedMessage = messageSource.getMessage("error.not.uploaded.file", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotUploadedMessage);
            }
            imagesPaths = imagesPaths.stream()
                    .map(s -> Base64.getEncoder().encodeToString(s.getBytes()))
                    .collect(Collectors.toList());
        }
//        List<String> imagesPathStr = imagesPaths.stream().map(s -> "\"" + s + "\"")
//                .collect(Collectors.toList());

        // price = product price + company profit
        Product productParams = Product.builder()
                .name(payload.getName())
//        (payload.getPrice()/100*payload.getCompanyProfit())
                .price(payload.getPrice())
                .quantity(payload.getQuantity())
                .companyProfit(payload.getCompanyProfit())
                .imagesCollection(GenericMapper.objectToJSONMapper(imagesPaths))
                .images(imagesPaths)
                .category(category)
                .vendor(vendor)
                .build();
        if(payload.getProductId()!=null)
            productParams.setProductId(payload.getProductId());
        if(payload.getDescription()!=null)
            productParams.setDescription(payload.getDescription());
        Product createdProduct = productRepository.save(productParams);
        createdProduct.setImages(productParams.getImages());
        if(payload.getProductId()==null&&(payload.getOptions()!=null&&!payload.getOptions().isEmpty())) {
            Product finalCreatedProduct = createdProduct;
            payload.getOptions().forEach(option ->{
                option.setProduct(finalCreatedProduct);
            });
            createdProduct.setOptions(optionRepository.saveAll(payload.getOptions()));
        }
        return createdProduct;
    }

    @Override
    public Boolean deleteProductById(Long productId) throws TabaldiGenericException {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Product", "المنتج");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Product product = productOptional.get();
            productRepository.deleteById(product.getProductId());
            return true;
        }
    }
    @Override
    public Product getProductById(Long productId) throws TabaldiGenericException, IOException {
        Optional<Product> selectedProduct = productRepository.findById(productId);
        if(!selectedProduct.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Product","المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        Product product = selectedProduct.get();
        product.setImages(GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class));
        return product;
    }

    @Override
    public Long countByCategory(Long categoryId) {
        return productRepository.countByCategory(categoryId);
    }

    @Override
    public List<CartItem> getProductCartItemsList(Long productId) throws TabaldiGenericException, IOException {
        Product product = this.getProductById(productId);
        List<CartItem> cartItems = cartItemRepository.findByProduct(product);
        if(cartItems.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Items","أغراض السلة");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (CartItem cartItem : cartItems.stream()
                .filter(cartItem -> cartItem.getOptionsCollection()!=null)
                .collect(Collectors.toList()))
            cartItem.setSelectedOptions(GenericMapper.jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
        return cartItems;
    }

    private boolean checkIfProductExistsPerVendor(Vendor selectedVendor, String name) {
        Optional<Product> product = productRepository.findByVendorAndName(selectedVendor.getVendorId(), name);
        return product.isPresent();
    }
}
