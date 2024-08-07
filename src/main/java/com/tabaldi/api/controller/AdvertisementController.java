package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.AdvertisementPayload;
import com.tabaldi.api.response.*;
import com.tabaldi.api.service.AdvertisementService;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/advertisements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdvertisementController {

    private final AdvertisementService advertisementService;
    private final MessageSource messageSource;

    @GetMapping("/{advertisementId}")
    public @ResponseBody ResponseEntity<AdvertisementResponse> getById (@PathVariable("advertisementId") Long advertisementId)
            throws TabaldiGenericException {
        Advertisement advertisement = advertisementService.getAdvertisementById(advertisementId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Advertisement", "الإعلان");

        return ResponseEntity.ok(AdvertisementResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .advertisement(advertisement).build());

    }

    @GetMapping
    public @ResponseBody ResponseEntity<ListResponse<Advertisement>> getAdvertisementsList () throws TabaldiGenericException {
        List<Advertisement> advertisementsList = advertisementService.getAdvertisementsList(); // may add filters
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Advertisements", "الإعلانات");
        return ResponseEntity.ok(
                ListResponse.<Advertisement>genericBuilder()
                        .list(advertisementsList)
                        .message(fetchMessage)
                        .build()
        );
    }

    @PostMapping(value = "/save", consumes = {"multipart/form-data"}, produces = "application/json")
    public @ResponseBody ResponseEntity<AdvertisementResponse> saveAdvertisement (
            @Valid @RequestParam(value = "AdvertisementPayload") final String payload,
            @Valid @RequestParam(value = "adsImage") final MultipartFile adsImage) throws TabaldiGenericException, IOException {

        AdvertisementPayload advertisementPayload = GenericMapper.jsonToObjectMapper(payload, AdvertisementPayload.class);
        Advertisement advertisement = advertisementService.saveAdvertisementInfo(advertisementPayload, adsImage);
        String event = advertisementPayload.getAdvertisementId()==null?"created":"updated";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "advertisement", "الإعلان", event, event.equals("created")?"حفظ":"تعديل");
        return ResponseEntity.ok(
                AdvertisementResponse.builder()
                        .event(event)
                        .advertisement(advertisement)
                        .message(successSaveMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{advertisementId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteAdvertisement (@PathVariable("advertisementId") Long advertisementId)
            throws TabaldiGenericException {
        Boolean isDeleted = advertisementService.deleteAdvertisementById(advertisementId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Advertisement", "الإعلان");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }

    @GetMapping("/toggle/showing/{advertisementId}")
    public @ResponseBody ResponseEntity<PublishResponse> toggleShownAdvertisement (@PathVariable("advertisementId") Long advertisementId)
            throws TabaldiGenericException {
        Boolean isShowing = advertisementService.toggleShownById(advertisementId);
        String successShowingMessage = MessagesUtils.getPublishMessage(messageSource, isShowing?"showed":"hidden", isShowing?"إظهار":"إخفاء", "Advertisement", "الإعلان");

        return ResponseEntity.ok(PublishResponse.builder()
                .message(successShowingMessage)
                .isPublished(isShowing).build());

    }
}
