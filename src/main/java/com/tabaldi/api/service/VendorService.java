package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.UserPayload;
import com.tabaldi.api.payload.VendorFrequency;
import com.tabaldi.api.payload.VendorPayload;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface VendorService {

    UserEntity addVendorUser(UserPayload payload) throws TabaldiGenericException;

    Vendor saveVendorInfo(VendorPayload payload, MultipartFile identityImage,
                          MultipartFile licenseImage, MultipartFile profileImage, MultipartFile coverImage) throws TabaldiGenericException;
    Boolean deleteVendorById(Long vendorId) throws TabaldiGenericException;
    Boolean toggleWorkingById(Long vendorId) throws TabaldiGenericException;
    List<Vendor> getVendorsList(String roleName) throws TabaldiGenericException;

    Vendor getVendorById(Long vendorId) throws TabaldiGenericException;
    Vendor getVendorByUserId(Long userId) throws TabaldiGenericException;
    Boolean deleteUserById(Long userId) throws TabaldiGenericException;
    List<Category> getVendorCategoriesList(Long vendorId) throws TabaldiGenericException;
    List<UserEntity> getVendorUsersList(Long vendorId) throws TabaldiGenericException;
    List<Advertisement> getVendorAdvertisementsList(Long vendorId) throws TabaldiGenericException;
    List<Order> getVendorOrdersList(Long vendorId) throws TabaldiGenericException, IOException;
    List<Product> getVendorProductsList(Long vendorId, String roleName) throws TabaldiGenericException, IOException;

    List<VendorFrequency> fetchFrequentVendorByOrders(List<Order> orders, int size) throws TabaldiGenericException;

    Long countAllProductsPerVendor(Long vendorId);

    Map<String, Object> searchLocation(String query) throws TabaldiGenericException;

    Map<String, Object> getLocationDetails(String placeId) throws TabaldiGenericException;
}
