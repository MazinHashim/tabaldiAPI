package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.UserPayload;
import com.tabaldi.api.payload.VendorPayload;
import com.tabaldi.api.payload.VerifyOtpPayload;
import com.tabaldi.api.response.*;
import com.tabaldi.api.service.InvoiceService;
import com.tabaldi.api.service.ProductService;
import com.tabaldi.api.service.UserService;
import com.tabaldi.api.service.VendorService;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VendorController {

    private final VendorService vendorService;
    private final ProductService productService;
    private final InvoiceService invoiceService;
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/{vendorId}")
    public @ResponseBody ResponseEntity<VendorResponse> getById (@PathVariable("vendorId") Long vendorId)
            throws TabaldiGenericException {
        Vendor vendor = vendorService.getVendorById(vendorId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Vendor", "البائع");

        return ResponseEntity.ok(VendorResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .vendor(vendor).build());

    }

    @GetMapping("/profile")
    public @ResponseBody ResponseEntity<VendorResponse> profile() throws TabaldiGenericException {
        Vendor vendor = vendorService.getProfile();
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Vendor", "البائع");
        return ResponseEntity.ok(
                VendorProfileResponse.builder()
                        .event("fetched")
                        .newUser(!userService.checkUserExistRegardlessOfRole(vendor.getUser()))
                        .vendor(vendor)
                        .message(fetchMessage)
                        .build()
        );
    }

    @GetMapping
    public @ResponseBody ResponseEntity<ListResponse<Vendor>> getVendorsList () throws TabaldiGenericException {
        List<Vendor> vendorsList = vendorService.getVendorsList(); // may add filters
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Vendors", "البائعين");
        return ResponseEntity.ok(
                ListResponse.<Vendor>genericBuilder()
                        .list(vendorsList)
                        .message(fetchMessage)
                        .build()
        );
    }

    @GetMapping("/{vendorId}/products")
    public @ResponseBody ResponseEntity<ListResponse<Product>> getProductsList (
            @PathVariable("vendorId") long vendorId) throws TabaldiGenericException, IOException {
        List<Product> productsList = vendorService.getVendorProductsList(vendorId); // may add filters
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Vendor Products", "منتجات البائع");
        return ResponseEntity.ok(
                ListResponse.<Product>genericBuilder()
                        .list(productsList)
                        .message(fetchMessage)
                        .build()
        );
    }

    @GetMapping("/{vendorId}/categories")
    public @ResponseBody ResponseEntity<ListResponse<VendorCategoryResponse>> getCategoriesList (
            @PathVariable("vendorId") long vendorId) throws TabaldiGenericException {
        List<Category> categoriesList = vendorService.getVendorCategoriesList(vendorId); // may add filters
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Vendor Categories", "أنواع منتجات البائع");
        List<VendorCategoryResponse> list = categoriesList.stream().map(category -> {
            Long count = productService.countByCategory(category.getCategoryId());
            return VendorCategoryResponse.builder()
                    .category(category)
                    .numberOfProducts(count.intValue())
                    .build();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(
                ListResponse.<VendorCategoryResponse>genericBuilder()
                        .list(list)
                        .message(fetchMessage)
                        .build()
        );
    }

    @GetMapping("/{vendorId}/orders")
    public @ResponseBody ResponseEntity<ListResponse<OrderMapper>> getOrdersList (
            @PathVariable("vendorId") long vendorId) throws TabaldiGenericException {
        List<Order> ordersList = vendorService.getVendorOrdersList(vendorId); // may add filters
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Customers Orders", "طلبات الزبائن");
        List<OrderMapper> vendorOrders = ordersList.stream()
                .map(order-> {
            order.getCartItems().forEach(cartItem -> {
                cartItem.getProduct().setOptions(null);
                cartItem.setCustomer(null);
            });
            return OrderMapper.mappedBuilder().order(order).build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(
                ListResponse.<OrderMapper>genericBuilder()
                        .list(vendorOrders)
                        .message(fetchMessage)
                        .build()
        );
    }
    @GetMapping("/{vendorId}/invoices")
    public @ResponseBody ResponseEntity<ListResponse<Invoice>> getInvoicesList (
            @PathVariable("vendorId") Long vendorId) throws TabaldiGenericException {
        List<Invoice> invoicesList = invoiceService.getInvoicesList(vendorId); // may add filters
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Invoice", "الفواتير");
        return ResponseEntity.ok(
                ListResponse.<Invoice>genericBuilder()
                        .list(invoicesList)
                        .message(fetchMessage)
                        .build()
        );
    }

    @PostMapping("/add/user")
    public @ResponseBody ResponseEntity<UserEntity> verifyAndAddVendor (
            @RequestBody @Valid UserPayload payload
    ) throws TabaldiGenericException {
        VerificationResponse verificationResponse = userService.verifyOtp(VerifyOtpPayload.builder()
                .phone(payload.getPhone())
                .keyRef(payload.getKeyRef())
                .allowRegistration(true)
                .otpCode(payload.getOtpCode())
                .build());
        UserVerification userVerification = verificationResponse.getUserVerification();
        UserEntity user = vendorService.addVendorUser(payload, userVerification);
        return ResponseEntity.ok(user);

    }

    @PostMapping(value = "/save", consumes = {"multipart/form-data"}, produces = "application/json")
    public @ResponseBody ResponseEntity<VendorResponse> saveVendor (
            @Valid @RequestParam(value = "VendorPayload") final String payload,
            @Valid @RequestParam(value = "identityImage") final MultipartFile identityImage,
            @Valid @RequestParam(value = "licenseImage") final MultipartFile licenseImage,
            @Valid @RequestParam(value = "profileImage", required = false) final MultipartFile profileImage,
            @Valid @RequestParam(value = "coverImage", required = false) final MultipartFile coverImage) throws TabaldiGenericException, IOException {
//        if(){
//
//        }
        VendorPayload vendorPayload = GenericMapper.jsonToObjectMapper(payload, VendorPayload.class);
        Vendor vendor = vendorService.saveVendorInfo(vendorPayload, identityImage, licenseImage, profileImage, coverImage);
        String event = vendorPayload.getVendorId()==null?"created":"updated";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "vendor", "التاجر", event, event.equals("created")?"حفظ":"تعديل");
        return ResponseEntity.ok(
                VendorResponse.builder()
                        .event(event)
                        .vendor(vendor)
                        .message(successSaveMessage)
                        .build()
        );
    }

    @DeleteMapping("/delete/{vendorId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteVendor (@PathVariable("vendorId") Long vendorId)
            throws TabaldiGenericException {
        Boolean isDeleted = vendorService.deleteVendorById(vendorId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Vendor", "التاجر");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }

    @GetMapping("/toggle/working/{vendorId}")
    public @ResponseBody ResponseEntity<PublishResponse> toggleWorkingVendor (@PathVariable("vendorId") Long vendorId)
            throws TabaldiGenericException, IOException {
        Boolean isWorking = vendorService.toggleWorkingById(vendorId);
        String successWorkingMessage = MessagesUtils.getPublishMessage(messageSource, isWorking?"opened":"closed", isWorking?"فتح":"إغلاق", "Vendor", "المتجر");

        return ResponseEntity.ok(PublishResponse.builder()
                .message(successWorkingMessage)
                .isPublished(isWorking).build());

    }
}
