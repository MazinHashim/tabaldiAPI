package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Advertisement;
import com.tabaldi.api.payload.AdvertisementPayload;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface AdvertisementService {
    List<Advertisement> getAdvertisementsList() throws TabaldiGenericException;
    Boolean toggleShownById(Long advertisementId) throws TabaldiGenericException;
    Advertisement getAdvertisementById(Long advertisementId) throws TabaldiGenericException;
    Advertisement saveAdvertisementInfo(AdvertisementPayload payload, MultipartFile adsImage) throws TabaldiGenericException;
    Boolean deleteAdvertisementById(Long advertisementId) throws TabaldiGenericException;
}
