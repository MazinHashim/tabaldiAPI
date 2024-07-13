package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.Option;
import com.tabaldi.api.model.Product;
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
        if(!selectedCartItem.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Item","عنصر سلة التسوق");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        CartItem cartItem = selectedCartItem.get();
        cartItem.setSelectedOptions(GenericMapper.jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
        return selectedCartItem.get();
    }

    @Override
    public CartItem saveCartItemInfo(CartItemPayload payload) throws TabaldiGenericException, IOException {

        Product selectedProduct = productService.getProductById(payload.getProductId());
        if(!selectedProduct.isPublished() || !selectedProduct.getCategory().isPublished()) {
            String notUnavailableMessage = messageSource.getMessage("error.item.unavailable", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notUnavailableMessage);
        }

        Customer selectedCustomer = customerService.getCustomerById(payload.getCustomerId());

        if(payload.getQuantity()>selectedProduct.getQuantity()) {
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
                long count = selectedOptionsGroups.size();
                long distinctCount = selectedOptionsGroups.stream().distinct().count();
                if (count != distinctCount || requiredOptions.size() != distinctCount) {
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
        return cartItemRepository.save(cartItemParams);
    }

    @Override
    public Boolean deleteCartItemById(Long cartItemId) throws TabaldiGenericException {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (!cartItemOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"CartItem", "عنصر سلة التسوق");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            CartItem cartItem = cartItemOptional.get();
            cartItemRepository.deleteById(cartItem.getItemId());
            return true;
        }
    }
}
