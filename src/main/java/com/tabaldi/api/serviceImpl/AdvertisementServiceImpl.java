package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Advertisement;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.payload.AdvertisementPayload;
import com.tabaldi.api.payload.FileDataObject;
import com.tabaldi.api.repository.AdvertisementRepository;
import com.tabaldi.api.service.AdvertisementService;
import com.tabaldi.api.service.FileStorageService;
import com.tabaldi.api.service.VendorService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final VendorService vendorService;
    private final MessageSource messageSource;
    private final FileStorageService fileStorageService;
    private final TabaldiConfiguration configuration;

    @Override
    public List<Advertisement> getAdvertisementsList() throws TabaldiGenericException {
        List<Advertisement> advertisementList = advertisementRepository.findAll();
        if(advertisementList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"advertisements", "الإعلانات");
            throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
        }
        return advertisementList;
    }

    @Override
    public Advertisement getAdvertisementById(Long advertisementId) throws TabaldiGenericException {
        Optional<Advertisement> selectedAdvertisement = advertisementRepository.findById(advertisementId);
        if(!selectedAdvertisement.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "advertisement","الإعلان");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedAdvertisement.get();
    }

    @Override
    public Advertisement saveAdvertisementInfo(AdvertisementPayload payload, MultipartFile adsImage) throws TabaldiGenericException {
        // update advertisement constraints
        String adsPath = "";
        boolean isShowing=false;
        if(payload.getAdvertisementId()!=null){
            Advertisement advertisement = this.getAdvertisementById(payload.getAdvertisementId());
            if(advertisement.getVendor()!=null && advertisement.getVendor().getVendorId()!=payload.getVendorId()){
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource,"advertisement", "الإعلان");
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
            } else {
                adsPath = advertisement.getAdsImage()!=null?advertisement.getAdsImage():"";
                isShowing = advertisement.isShown();
            }
        }
        Vendor selectedVendor=null;
        if(payload.getVendorId()!=null) {
            selectedVendor = vendorService.getVendorById(payload.getVendorId());
        }
        if(payload.getUrl() == null && selectedVendor==null){
            String requiredOneOfMessage = messageSource.getMessage("error.required.one.of.them", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredOneOfMessage);
        }
        if(payload.getVendorId() == null && adsImage.isEmpty()){
            String requiredImageUploadMessage = messageSource.getMessage("error.required.upload.file", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredImageUploadMessage);
        }
        if (!adsPath.isEmpty()) {
            List urlList = new ArrayList<String>();
            if(!adsImage.isEmpty() && !adsPath.isEmpty()) urlList.add(new String(Base64.getDecoder().decode(adsPath.getBytes())));
            fileStorageService.remove(urlList);
        }
        if (!adsImage.isEmpty()) {
            if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(adsImage.getContentType())) {
                String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
            }
            adsPath = configuration.getHostAdsImageFolder()
                    .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                    .concat(adsImage.getOriginalFilename()
                            .substring(adsImage.getOriginalFilename().indexOf(".")));
        }
        List<FileDataObject> addList = new ArrayList();
        if(!adsImage.isEmpty()) addList.add(new FileDataObject(adsImage, adsPath));
        Boolean saved = fileStorageService.save(addList);
        if(!saved){
            String imageNotUploadedMessage = messageSource.getMessage("error.not.uploaded.file", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotUploadedMessage);
        }
        Advertisement advertisementParams = Advertisement.builder()
                .title(payload.getTitle())
                .isShown(true)
                .expireIn(OffsetDateTime.now().plusDays(10))
                .build();
        if(payload.getUrl() != null && selectedVendor==null) {
            advertisementParams.setUrl(payload.getUrl());
        } else {
            advertisementParams.setVendor(selectedVendor);
        }
        if(payload.getSubtitle() != null){
            advertisementParams.setSubTitle(payload.getSubtitle());
        }
        if (payload.getAdvertisementId() != null) {
            advertisementParams.setAdvertisementId(payload.getAdvertisementId());
            advertisementParams.setShown(isShowing);
            if(adsPath.contains(configuration.getHostAdsImageFolder())) advertisementParams.setAdsImage(Base64.getEncoder().encodeToString(adsPath.getBytes()));
            else advertisementParams.setAdsImage(adsPath);
        } else {
            advertisementParams.setAdsImage(Base64.getEncoder().encodeToString(adsPath.getBytes()));
        }
        return advertisementRepository.save(advertisementParams);
    }
    @Override
    public Boolean toggleShownById(Long advertisementId) throws TabaldiGenericException {
        Advertisement advertisement = this.getAdvertisementById(advertisementId);
        int updated = advertisementRepository.toggleShownById(!advertisement.isShown(), advertisement.getAdvertisementId());
        if(updated>0)
            return !advertisement.isShown();
        return advertisement.isShown();
    }

    @Override
    public Boolean deleteAdvertisementById(Long advertisementId) throws TabaldiGenericException {
        Optional<Advertisement> advertisementOptional = advertisementRepository.findById(advertisementId);
        if (!advertisementOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"advertisement", "الإعلان");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Advertisement advertisement = advertisementOptional.get();
            List<String> list = List.of(advertisement.getAdsImage());
            fileStorageService.remove(list.stream()
                    .map(path -> new String(Base64.getDecoder().decode(path.getBytes()))).collect(Collectors.toList()));
            advertisementRepository.deleteById(advertisement.getAdvertisementId());
            return true;
        }
    }
}
