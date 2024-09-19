package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.CartItemPayload;
import com.tabaldi.api.repository.CartItemRepository;
import com.tabaldi.api.repository.ProductRepository;
import com.tabaldi.api.service.CartItemService;
import com.tabaldi.api.service.CustomerService;
import com.tabaldi.api.service.ProductService;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final CustomerService customerService;
    private final MessageSource messageSource;

    @Override
    public CartItem getCartItemById(Long cartItemId) throws TabaldiGenericException, IOException {
        Optional<CartItem> selectedCartItem = cartItemRepository.findById(cartItemId);
        if (!selectedCartItem.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Item", "عنصر سلة التسوق");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        CartItem cartItem = selectedCartItem.get();
        if (cartItem.getSelectedOptions() != null && !cartItem.getSelectedOptions().isEmpty()){
            cartItem.setSelectedOptions(GenericMapper.jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
        }
        cartItem.getProduct().setImages(GenericMapper.jsonToListObjectMapper(cartItem.getProduct().getImagesCollection(), String.class));

        return selectedCartItem.get();
    }
    @Override
    public List<CartItem> updateQuantityById(Long cartItemId, int newQuantity) throws TabaldiGenericException, IOException {
        Optional<CartItem> selectedCartItem = cartItemRepository.findById(cartItemId);
        if(!selectedCartItem.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Item","عنصر سلة التسوق");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        CartItem cartItem = selectedCartItem.get();
        cartItem.setQuantity(newQuantity);
        if(cartItem.getOrder()!=null){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Item","عنصر سلة التسوق");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        cartItem.setSelectedOptions(GenericMapper.jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
        // Save the updated cart item to the repository
        cartItemRepository.save(cartItem);

        // Use streams to find and update the cart item in the list
        List<CartItem> updatedCart =  customerService.getCustomerActiveCartItemsList(cartItem.getCustomer().getCustomerId(), true);
        updatedCart.stream()
            .filter(item -> item.getItemId()==cartItemId)
            .findFirst()
            .ifPresent(item -> {
                int index = updatedCart.indexOf(item);
                updatedCart.set(index, cartItem);
            });
        return updatedCart;
    }

    @Override
    public List<CartItem> saveCartItemInfo(CartItemPayload payload) throws TabaldiGenericException, IOException {

        Product selectedProduct = productService.getProductById(payload.getProductId());
        if(!selectedProduct.isPublished() || !selectedProduct.getCategory().isPublished()) {
            String notUnavailableMessage = messageSource.getMessage("error.item.unavailable", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notUnavailableMessage);
        }

        Customer selectedCustomer = customerService.getCustomerById(payload.getCustomerId());
        List<CartItem> cartList = customerService.getCustomerActiveCartItemsList(selectedCustomer.getCustomerId(), false);
        List<Vendor> vendors = cartList.stream()
                .map(cartItem -> cartItem.getProduct().getVendor())
                .distinct().collect(Collectors.toList());
        boolean isAllNotRestaurant = vendors.stream()
                .allMatch(vendor -> !vendor.getVendorType().equals(VendorType.RESTAURANT));
        boolean isAllRestaurant = vendors.stream()
                .allMatch(vendor -> vendor.getVendorType().equals(VendorType.RESTAURANT));
        if(!(isAllNotRestaurant || isAllRestaurant)){
            String onlyOneAllowedMessage = messageSource.getMessage("error.separate.restaurant.order", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, onlyOneAllowedMessage);
        }

        if(payload.getQuantity() > selectedProduct.getQuantity()) {
            String notAvailableMessage = messageSource.getMessage("error.not.available.qnt", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notAvailableMessage);
        }
        List<Option> selectedOptions = null;
        if(!selectedProduct.getOptions().isEmpty()) {
            List<Option> requiredOptions = selectedProduct.getOptions().stream()
                    .filter((option -> option.getGroupFlag() != null && option.getFee() == null))
                    .collect(Collectors.toList());
            selectedOptions = GenericMapper.jsonToListObjectMapper(
                    payload.getOptions()!=null?payload.getOptions():"[]", Option.class);
            if (!requiredOptions.isEmpty()) {
                List<String> selectedOptionsGroups = selectedOptions.stream()
                        .filter(option -> option.getGroupFlag()!=null)
                        .map((option -> option.getGroupFlag()))
                        .collect(Collectors.toList());
                long selectedCount = selectedOptionsGroups.size();
                long requiredCount = requiredOptions.stream()
                        .map((option -> option.getGroupFlag())).distinct().count();
                if (selectedCount != requiredCount) {
                    String requiredOptionsMessage = messageSource.getMessage("error.one.option.required", null, LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredOptionsMessage);
                }
            }
        } else {
            if(payload.getOptions() != null) {
                String requiredOptionsMessage = messageSource.getMessage("error.no.product.options", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredOptionsMessage);
            }
        }
        CartItem cartItemParams = CartItem.builder()
                .price(payload.getPrice())
                .quantity(payload.getQuantity())
                .product(selectedProduct)
                .optionsCollection(payload.getOptions())
                .customer(selectedCustomer)
                .build();
        if (payload.getComment() != null || !payload.getComment().isEmpty())
            cartItemParams.setComment(payload.getComment());
        if(selectedOptions!=null)
            cartItemParams.setSelectedOptions(selectedOptions);
        cartList.add(cartItemRepository.saveAndFlush(cartItemParams));
        return cartList;
    }

    @Override
    public List<CartItem> deleteCartItemById(Long cartItemId) throws TabaldiGenericException, IOException {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (!cartItemOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"CartItem", "عنصر سلة التسوق");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            CartItem cartItem = cartItemOptional.get();
            if(cartItem.getOrder()!=null){
                String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"CartItem", "عنصر سلة التسوق");
                throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
            }else {
                cartItemRepository.deleteById(cartItem.getItemId());
                return customerService.getCustomerActiveCartItemsList(cartItem.getCustomer().getCustomerId(), true);
//            return activeCart.stream().filter(cartItem1 -> cartItem1.getItemId()!=cartItemId)
//                    .collect(Collectors.toList());
            }
        }
    }
}
