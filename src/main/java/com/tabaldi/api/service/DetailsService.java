package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Address;
import com.tabaldi.api.payload.AddressPayload;
import com.tabaldi.api.response.AdminHomeDetails;
import com.tabaldi.api.response.AdminHomeDetailsResponse;
import com.tabaldi.api.response.VendorHomeDetails;
import org.springframework.stereotype.Service;

@Service
public interface DetailsService {

    AdminHomeDetails getAdminHomeDetails() throws TabaldiGenericException;
    VendorHomeDetails getVendorHomeDetails(Long vendorId) throws TabaldiGenericException;
}
