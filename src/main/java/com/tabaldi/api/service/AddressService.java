package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Address;
import com.tabaldi.api.model.Address;
import com.tabaldi.api.payload.AddressPayload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AddressService {

    Address getAddressById(Long addressId) throws TabaldiGenericException;
    Address saveAddressInfo(AddressPayload payload) throws TabaldiGenericException;
    Boolean deleteAddressById(Long addressId) throws TabaldiGenericException;
    Address changeSelectedAddress(Long addressId) throws TabaldiGenericException;
}
