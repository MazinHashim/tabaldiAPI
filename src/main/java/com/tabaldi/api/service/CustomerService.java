package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Address;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.payload.CustomerFrequency;
import com.tabaldi.api.payload.CustomerPayload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface CustomerService {
    Customer saveCustomerInfo(CustomerPayload payload) throws TabaldiGenericException;

    Boolean deleteCustomerById(Long customerId) throws TabaldiGenericException;

    Customer getCustomerById(Long customerId) throws TabaldiGenericException;
    Address getSelectedCustomerAddress(Long customerId) throws TabaldiGenericException;
    List<CartItem> getCustomerCartItemsList(Long customerId) throws TabaldiGenericException, IOException;
    Boolean clearCustomerCartItems(Long customerId) throws TabaldiGenericException;
    List<CartItem> getCustomerActiveCartItemsList(Long customerId, boolean check) throws TabaldiGenericException, IOException;
    List<Address> getCustomerAddressesList(Long customerId) throws TabaldiGenericException;
    Customer getCustomerByUserId(Long userId) throws TabaldiGenericException;
    Customer getProfile() throws TabaldiGenericException;
    Map<String, Long> countAllCustomerInSystem();
    List<CustomerFrequency> fetchFrequentCustomerByOrders(List<Order> orders, int size);
}
