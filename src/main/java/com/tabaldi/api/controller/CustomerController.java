package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Address;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.payload.CustomerPayload;
import com.tabaldi.api.response.*;
import com.tabaldi.api.service.AddressService;
import com.tabaldi.api.service.CustomerService;
import com.tabaldi.api.service.UserService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;
    private final MessageSource messageSource;
    private final UserService userService;

    @GetMapping("/{customerId}")
    public @ResponseBody ResponseEntity<CustomerResponse> getById (@PathVariable("customerId") Long customerId)
            throws TabaldiGenericException {
        Customer customer = customerService.getCustomerById(customerId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Customer", "الزيون");

        return ResponseEntity.ok(CustomerResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .customer(customer).build());

    }

    @PostMapping("/save")
    public @ResponseBody ResponseEntity<CustomerResponse> saveCustomer (
            @RequestBody @Valid CustomerPayload payload) throws TabaldiGenericException {

        Customer customer = customerService.saveCustomerInfo(payload);
        String event = payload.getCustomerId()==null?"created":"updated";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "Customer", "الزيون", event, event.equals("created")?"حفظ":"تعديل");
        return ResponseEntity.ok(
                CustomerResponse.builder()
                        .event(event)
                        .customer(customer)
                        .message(successSaveMessage)
                        .build()
        );
    }
    @GetMapping("/profile")
    public @ResponseBody ResponseEntity<CustomerResponse> profile() throws TabaldiGenericException {
        Customer customer = customerService.getProfile();
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Customer", "الزبون");
        return ResponseEntity.ok(
                CustomerProfileResponse.builder()
                        .event("fetched")
                        .newUser(!userService.checkUserExistRegardlessOfRole(customer.getUser()))
                        .customer(customer)
                        .message(fetchMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{customerId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteCustomer (@PathVariable("customerId") Long customerId)
            throws TabaldiGenericException {
        Boolean isDeleted = customerService.deleteCustomerById(customerId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Customer", "الزيون");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }

    @GetMapping("/{customerId}/addresses")
    public @ResponseBody ResponseEntity<ListResponse<Address>> getCustomerAddressesList (
            @PathVariable("customerId") Long customerId) throws TabaldiGenericException {
        List<Address> addressesList = customerService.getCustomerAddressesList(customerId);
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Addresses", "العناوين");
        return ResponseEntity.ok(
                ListResponse.<Address>genericBuilder()
                        .list(addressesList)
                        .message(fetchMessage)
                        .build()
        );
    }
    @GetMapping("/{customerId}/cartItems")
    public @ResponseBody ResponseEntity<ListResponse<CartItem>> getCustomerCartItemsList (
            @PathVariable("customerId") Long customerId) throws TabaldiGenericException, IOException {
        List<CartItem> cartItemsList = customerService.getCustomerCartItemsList(customerId);
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Cart Items", "أغراض السلة");
        return ResponseEntity.ok(
                ListResponse.<CartItem>genericBuilder()
                        .list(cartItemsList)
                        .message(fetchMessage)
                        .build()
        );
    }
    @GetMapping("/{customerId}/active/cartItems")
    public @ResponseBody ResponseEntity<ListResponse<CartItem>> getCustomerActiveCartItemsList (
            @PathVariable("customerId") Long customerId) throws TabaldiGenericException, IOException {
        List<CartItem> cartItemsList = customerService.getCustomerActiveCartItemsList(customerId);
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Cart Items", "أغراض السلة");
        return ResponseEntity.ok(
                ListResponse.<CartItem>genericBuilder()
                        .list(cartItemsList)
                        .message(fetchMessage)
                        .build()
        );
    }
    @DeleteMapping("/{customerId}/clear/cart")
    public @ResponseBody ResponseEntity<DeleteResponse> clearCustomerCartItems (
            @PathVariable("customerId") Long customerId) throws TabaldiGenericException, IOException {
        Boolean isDeleted = customerService.clearCustomerCartItems(customerId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Cart Items", "أغراض السلة");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());
    }
}
