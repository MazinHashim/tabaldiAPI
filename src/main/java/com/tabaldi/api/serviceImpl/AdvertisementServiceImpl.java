package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Advertisement;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.model.VendorType;
import com.tabaldi.api.payload.AdvertisementPayload;
import com.tabaldi.api.payload.FileDataObject;
import com.tabaldi.api.repository.AdvertisementRepository;
import com.tabaldi.api.service.AdvertisementService;
import com.tabaldi.api.service.FileStorageService;
import com.tabaldi.api.service.VendorService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        if (advertisementList.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "advertisements", "الإعلانات");
            throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
        }
        return advertisementList.stream()
                .sorted(Comparator.comparing(Advertisement::getPriority).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Advertisement> getActiveAdvertisementsList() throws TabaldiGenericException {
        List<Advertisement> advertisementList = advertisementRepository.findByIsShownAndExpireDateGreaterThan(true,
                LocalDate.now());
        if (advertisementList.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "advertisements", "الإعلانات");
            throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
        }
        return advertisementList.stream()
                .sorted(Comparator.comparing(Advertisement::getPriority).reversed())
                .sorted(Comparator.comparing(Advertisement::getExpireDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Advertisement getAdvertisementById(Long advertisementId) throws TabaldiGenericException {
        Optional<Advertisement> selectedAdvertisement = advertisementRepository.findById(advertisementId);
        if (!selectedAdvertisement.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "advertisement", "الإعلان");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedAdvertisement.get();
    }

    @Override
    @Transactional
    public List<Advertisement> saveAdvertisementInfo(AdvertisementPayload payload,
            @Valid MultipartFile adsImage1
    // @Valid MultipartFile adsImage2,
    // @Valid MultipartFile adsImage3
    ) throws TabaldiGenericException {
        // update advertisement constraints
        String adsPath1 = "";
        // String adsPath2 = "";
        // String adsPath3 = "";
        boolean isShowing = false;
        Advertisement advertisement = null;
        if (payload.getAdvertisementId() != null) {
            advertisement = this.getAdvertisementById(payload.getAdvertisementId());
            if (advertisement.getVendor() != null && advertisement.getVendor().getVendorId() != payload.getVendorId()) {
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource, "advertisement",
                        "الإعلان");
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
            } else {
                adsPath1 = advertisement.getAdsImage1() != null ? advertisement.getAdsImage1() : "";
                // adsPath2 =
                // advertisement.getAdsImage2()!=null?advertisement.getAdsImage2():"";
                // adsPath3 =
                // advertisement.getAdsImage3()!=null?advertisement.getAdsImage3():"";
                isShowing = advertisement.isShown();
            }
        }
        if (payload.getCreateDate().isAfter(payload.getExpireDate())) {
            String invalidDateMessage = messageSource.getMessage("error.invalid.date.exceed", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, invalidDateMessage);
        }
        Vendor selectedVendor = null;
        if (payload.getVendorId() != null) {
            selectedVendor = vendorService.getVendorById(payload.getVendorId());
        }
        this.validateAndEnsureUniquePriorityReplacingIfNecessary(payload.getPriority(), selectedVendor, advertisement,
                payload.isReplacePriority());
        if ((payload.getUrl() != null && !payload.getUrl().isEmpty()) && selectedVendor != null) {
            String requiredOneOfMessage = messageSource.getMessage("error.required.one.of.them", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredOneOfMessage);
        }
        if (payload.getAdvertisementId() == null && (adsImage1.isEmpty())) {
            // ||adsImage2.isEmpty()||adsImage3.isEmpty())){
            String requiredImageUploadMessage = messageSource.getMessage("error.required.upload.file", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredImageUploadMessage);
        }
        if (!adsPath1.isEmpty()) {
            // || !adsPath2.isEmpty() || !adsPath3.isEmpty()) {
            List<String> urlList = new ArrayList<String>();
            if (!adsImage1.isEmpty() && !adsPath1.isEmpty())
                urlList.add(new String(Base64.getDecoder().decode(adsPath1.getBytes())));
            // if(!adsImage2.isEmpty() && !adsPath2.isEmpty()) urlList.add(new
            // String(Base64.getDecoder().decode(adsPath2.getBytes())));
            // if(!adsImage3.isEmpty() && !adsPath3.isEmpty()) urlList.add(new
            // String(Base64.getDecoder().decode(adsPath3.getBytes())));
            fileStorageService.remove(urlList);
        }
        adsPath1 = this.checkAndGenerateImagePath(adsImage1, adsPath1);
        // adsPath2 = this.checkAndGenerateImagePath(adsImage2, adsPath2);
        // adsPath3 = this.checkAndGenerateImagePath(adsImage3, adsPath3);
        List<FileDataObject> addList = new ArrayList<FileDataObject>();
        if (!adsImage1.isEmpty())
            addList.add(new FileDataObject(adsImage1, adsPath1));
        // if(!adsImage2.isEmpty()) addList.add(new FileDataObject(adsImage2,
        // adsPath2));
        // if(!adsImage3.isEmpty()) addList.add(new FileDataObject(adsImage3,
        // adsPath3));
        Boolean saved = fileStorageService.save(addList);
        if (!saved) {
            String imageNotUploadedMessage = messageSource.getMessage("error.not.uploaded.file", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotUploadedMessage);
        }
        Advertisement advertisementParams = Advertisement.builder()
                .title(payload.getTitle())
                .arTitle(payload.getArTitle())
                .createdDate(payload.getCreateDate())
                .expireDate(payload.getExpireDate())
                .startTime(payload.getStartTime())
                .priority(payload.getPriority())
                .endTime(payload.getEndTime())
                .build();
        if (payload.getUrl() != null && selectedVendor == null) {
            advertisementParams.setUrl(payload.getUrl());
        } else {
            advertisementParams.setVendor(selectedVendor);
        }
        if (payload.getSubtitle() != null) {
            advertisementParams.setSubtitle(payload.getSubtitle());
        }
        if (payload.getArSubtitle() != null) {
            advertisementParams.setArSubtitle(payload.getArSubtitle());
        }
        if (payload.getAdvertisementId() != null) {
            advertisementParams.setAdvertisementId(payload.getAdvertisementId());
            advertisementParams.setShown(isShowing);
            if (adsPath1.contains(configuration.getHostAdsImageFolder()))
                advertisementParams.setAdsImage1(Base64.getEncoder().encodeToString(adsPath1.getBytes()));
            else
                advertisementParams.setAdsImage1(adsPath1);
            // if(adsPath2.contains(configuration.getHostAdsImageFolder()))
            // advertisementParams.setAdsImage2(Base64.getEncoder().encodeToString(adsPath2.getBytes()));
            // else advertisementParams.setAdsImage2(adsPath2);
            // if(adsPath3.contains(configuration.getHostAdsImageFolder()))
            // advertisementParams.setAdsImage3(Base64.getEncoder().encodeToString(adsPath3.getBytes()));
            // else advertisementParams.setAdsImage3(adsPath3);
        } else {
            advertisementParams.setShown(true);
            advertisementParams.setAdsImage1(Base64.getEncoder().encodeToString(adsPath1.getBytes()));
            // advertisementParams.setAdsImage2(Base64.getEncoder().encodeToString(adsPath2.getBytes()));
            // advertisementParams.setAdsImage3(Base64.getEncoder().encodeToString(adsPath3.getBytes()));
        }
        List<Advertisement> advertisements = this.getAdvertisementsList().stream()
                .filter(ad -> payload.getAdvertisementId() != null
                        && ad.getAdvertisementId() != payload.getAdvertisementId())
                .collect(Collectors.toList());
        advertisements.add(advertisementRepository.save(advertisementParams));
        return advertisements;
    }

    private String checkAndGenerateImagePath(MultipartFile adsImage1, String path) throws TabaldiGenericException {
        if (!adsImage1.isEmpty()) {
            if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(adsImage1.getContentType())) {
                String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null,
                        LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
            }
            return configuration.getHostAdsImageFolder()
                    .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                    .concat(adsImage1.getOriginalFilename()
                            .substring(adsImage1.getOriginalFilename().indexOf(".")));
        }
        return path;
    }

    private void validateAndEnsureUniquePriorityReplacingIfNecessary(Integer priority, Vendor vendor,
            Advertisement advertisement,
            boolean isReplacePriority)
            throws TabaldiGenericException {
        List<Advertisement> existingAds;
        if (vendor == null) {
            existingAds = advertisementRepository.findByVendorIsNull();
        } else {
            existingAds = advertisementRepository.findByVendorVendorType(vendor.getVendorType());
        }
        boolean priorityExists = existingAds.stream()
                .filter(ad -> ad
                        .getAdvertisementId() != (advertisement == null ? null : advertisement.getAdvertisementId()))
                .anyMatch(ad -> ad.getPriority() == priority);

        String errorMessage = messageSource.getMessage("error.duplicate.priority", null,
                LocaleContextHolder.getLocale());

        // Handle priority conflicts when adding a new advertisement or updating an
        // existing one
        if (priorityExists) {
            if (!isReplacePriority) {
                // Throw an exception if trying to add a new ad with existing priority or update
                // with a conflicting priority
                if (advertisement == null || (advertisement != null && advertisement.getPriority() != priority)) {
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
                }
            } else if (advertisement == null || (advertisement != null && advertisement.getPriority() != priority)) {
                // Replace existing advertisement with the same priority
                if (vendor == null) {
                    advertisementRepository.deleteByPriorityAndVendorIsNull(priority);
                } else {
                    advertisementRepository.deleteByPriorityAndVendorVendorType(priority, vendor.getVendorType());
                }
            }
        }
    }

    @Override
    public Boolean toggleShownById(Long advertisementId) throws TabaldiGenericException {
        Advertisement advertisement = this.getAdvertisementById(advertisementId);
        int updated = advertisementRepository.toggleShownById(!advertisement.isShown(),
                advertisement.getAdvertisementId());
        if (updated > 0)
            return !advertisement.isShown();
        return advertisement.isShown();
    }

    @Override
    public Boolean deleteAdvertisementById(Long advertisementId) throws TabaldiGenericException {
        Optional<Advertisement> advertisementOptional = advertisementRepository.findById(advertisementId);
        if (!advertisementOptional.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "advertisement", "الإعلان");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Advertisement advertisement = advertisementOptional.get();
            List<String> list = List.of(
                    advertisement.getAdsImage1());
            // advertisement.getAdsImage2(),
            // advertisement.getAdsImage3());
            fileStorageService.remove(list.stream()
                    .map(path -> new String(Base64.getDecoder().decode(path.getBytes()))).collect(Collectors.toList()));
            advertisementRepository.deleteById(advertisement.getAdvertisementId());
            return true;
        }
    }

    @Override
    public Map<String, String> getAvailablePriorities(int maxPriority) throws TabaldiGenericException {
        List<Advertisement> allAdvertisements = advertisementRepository.findAll();
        if (allAdvertisements.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "advertisements", "الإعلانات");
            throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
        }

        Map<String, String> availablePriorities = new HashMap<>();

        // Group advertisements by vendor type and non-vendor
        Map<String, List<Advertisement>> groupedAds = allAdvertisements.stream()
                .collect(Collectors.groupingBy(
                        ad -> ad.getVendor() == null ? "EXTERNAL_ADS" : ad.getVendor().getVendorType().name()));

        // Calculate available priorities for each group
        for (Map.Entry<String, List<Advertisement>> entry : groupedAds.entrySet()) {
            String key = entry.getKey();
            List<Integer> usedPrioritiesForGroup = entry.getValue().stream()
                    .map(Advertisement::getPriority)
                    .collect(Collectors.toList());

            String availablePrioritiesForGroup = IntStream.rangeClosed(1, maxPriority)
                    .filter(i -> !usedPrioritiesForGroup.contains(i))
                    .mapToObj(i -> "إعلان " + i)
                    .collect(Collectors.joining(" | "));

            availablePriorities.put(key, availablePrioritiesForGroup);
        }
        // Add entries for vendor types that don't have any advertisements
        for (VendorType vendorType : VendorType.values()) {
            if (!availablePriorities.containsKey(vendorType.name())) {
                availablePriorities.put(vendorType.name(), IntStream.rangeClosed(1, maxPriority)
                        .mapToObj(i -> "إعلان " + i)
                        .collect(Collectors.joining(" | ")));
            }
        }

        // Add entry for EXTERNAL_ADS if it doesn't exist
        if (!availablePriorities.containsKey("EXTERNAL_ADS")) {
            availablePriorities.put("EXTERNAL_ADS", IntStream.rangeClosed(1, maxPriority)
                    .mapToObj(i -> "إعلان " + i)
                    .collect(Collectors.joining(" | ")));
        }

        return availablePriorities;
    }
}
