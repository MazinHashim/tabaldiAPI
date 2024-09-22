package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Address;
import com.tabaldi.api.payload.AddressPayload;
import com.tabaldi.api.response.*;
import com.tabaldi.api.service.AddressService;
import com.tabaldi.api.service.DetailsService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/details")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetailsController {

    private final DetailsService detailsService;
    private final MessageSource messageSource;

    @GetMapping("/admin/home")
    public @ResponseBody ResponseEntity<AdminHomeDetailsResponse> getAdminHomeDetails ()
            throws TabaldiGenericException, IOException {
        AdminHomeDetails details = detailsService.getAdminHomeDetails();
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Home details", "تفاصيل الحساب");

        return ResponseEntity.ok(AdminHomeDetailsResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .details(details).build());

    }

    @GetMapping("/vendor/home/{vendorId}")
    public @ResponseBody ResponseEntity<VendorHomeDetailsResponse> getVendorHomeDetails (@PathVariable("vendorId") Long vendorId)
            throws TabaldiGenericException, IOException {
        VendorHomeDetails details = detailsService.getVendorHomeDetails(vendorId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Home details", "تفاصيل الحساب");

        return ResponseEntity.ok(VendorHomeDetailsResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .details(details).build());

    }
}
