package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Address;
import com.tabaldi.api.payload.AddressPayload;
import com.tabaldi.api.response.AddressResponse;
import com.tabaldi.api.response.DeleteResponse;
import com.tabaldi.api.service.AddressService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AddressController {

    private final AddressService addressService;
    private final MessageSource messageSource;

    @GetMapping("/{addressId}")
    public @ResponseBody ResponseEntity<AddressResponse> getById (@PathVariable("addressId") Long addressId)
            throws TabaldiGenericException {
        Address address = addressService.getAddressById(addressId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Address", "العنوان");

        return ResponseEntity.ok(AddressResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .address(address).build());

    }

    @PostMapping("/save")
    public @ResponseBody ResponseEntity<AddressResponse> saveAddress (
            @RequestBody @Valid AddressPayload payload) throws TabaldiGenericException {

        Address address = addressService.saveAddressInfo(payload);
        String event = payload.getAddressId()==null?"created":"updated";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "Address", "العنوان", event, event.equals("created")?"حفظ":"تعديل");
        return ResponseEntity.ok(
                AddressResponse.builder()
                        .event(event)
                        .address(address)
                        .message(successSaveMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{addressId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteAddress (@PathVariable("addressId") Long addressId)
            throws TabaldiGenericException {
        Boolean isDeleted = addressService.deleteAddressById(addressId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Address", "العنوان");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }
    @GetMapping("/select/{addressId}")
    public @ResponseBody ResponseEntity<AddressResponse> selectAddress (@PathVariable("addressId") Long addressId)
            throws TabaldiGenericException {
        Address selectedAddress = addressService.changeSelectedAddress(addressId);
        String successSelectedMessage = MessagesUtils.getFetchMessage(messageSource, "Address", "العنوان");

        return ResponseEntity.ok(AddressResponse.builder()
                .event("selected")
                .message(successSelectedMessage)
                .address(selectedAddress).build());

    }
}
