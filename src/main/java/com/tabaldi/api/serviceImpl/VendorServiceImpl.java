package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.*;
import com.tabaldi.api.repository.*;
import com.tabaldi.api.service.*;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;

    private final MessageSource messageSource;

    private final FileStorageService fileStorageService;
    private final SequencesService sequencesService;
    private final UserService userService;
    private final SessionService sessionService;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final TabaldiConfiguration configuration;


    @Override
    public List<Vendor> getVendorsList() throws TabaldiGenericException {
        List<Vendor> vendorList = vendorRepository.findAll();
        if(vendorList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"vendors", "البائعين");
            throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
        }
        return vendorList;
    }

    @Override
    public UserEntity addVendorUser(UserPayload payload, UserVerification userVerification) throws TabaldiGenericException {

        Optional<UserEntity> userOptional = userRepository.findByPhone(payload.getPhone());
        UserEntity user;
        if(!userOptional.isPresent()){
            user = UserEntity.builder()
                    .phone(payload.getPhone())
                    .email(payload.getEmail())
                    .agreeTermsConditions(payload.isAgreeTermsConditions())
                    .role(Role.VENDOR)
                    .build();
            user = userRepository.saveAndFlush(user);
        } else {
            userVerification.setStatus(VerificationStatus.EXPIRED);
            userVerification.setVerifiedTime(null);
            userVerificationRepository.save(userVerification);
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"User", "المستخدم");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        }
        if(userVerification!=null){
            userVerification.setUser(user);
            userVerificationRepository.save(userVerification);
        }
        return user;
    }
    @Override
    @Transactional
    public Vendor saveVendorInfo(VendorPayload payload, MultipartFile identityImage,
                                 MultipartFile licenseImage, MultipartFile profileImage) throws TabaldiGenericException {
        String licensePath = "";
        String identityPath = "";
        String profilePath = "";
        // update vendor constraints
        if (payload.getVendorId() != null) {
            Vendor vendor = this.getVendorById(payload.getVendorId());
            if (vendor.getUser() != null && vendor.getUser().getUserId() != payload.getUserId()) {
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource, "Vendor", "التاجر");
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
            } else {
                licensePath = vendor.getLicenseImage()!=null?vendor.getLicenseImage():"";
                identityPath = vendor.getIdentityImage()!=null?vendor.getIdentityImage():"";
                profilePath = vendor.getProfileImage()!=null?vendor.getProfileImage():"";
            }
        }
        UserEntity user = userService.getUserById(payload.getUserId());
        if(!user.getRole().equals(Role.VENDOR)){
            String mismatchMessage = MessagesUtils.getMismatchRoleMessage(messageSource, "Vendor","البائع");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, mismatchMessage);
        }
        if(payload.getVendorId() == null && (identityImage.isEmpty()||licenseImage.isEmpty())){
            String requiredImageUploadMessage = messageSource.getMessage("error.required.upload.file", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredImageUploadMessage);
        }
        if (!profilePath.isEmpty()||!identityPath.isEmpty()||!licensePath.isEmpty()) {
            List urlList = new ArrayList<String>();
            if(!profileImage.isEmpty() && !profilePath.isEmpty()) urlList.add(new String(Base64.getDecoder().decode(profilePath.getBytes())));
            if(!identityImage.isEmpty() && !identityPath.isEmpty()) urlList.add(new String(Base64.getDecoder().decode(identityPath.getBytes())));
            if(!licenseImage.isEmpty() && !licensePath.isEmpty()) urlList.add(new String(Base64.getDecoder().decode(licensePath.getBytes())));
            fileStorageService.remove(urlList);
        }
        if (!profileImage.isEmpty()) {
            if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(profileImage.getContentType())) {
                String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
            }
            profilePath = configuration.getHostProductImageFolder()
                    .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                    .concat(profileImage.getOriginalFilename()
                            .substring(profileImage.getOriginalFilename().indexOf(".")));
        }
        if (!identityImage.isEmpty()) {
            if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(identityImage.getContentType())) {
                String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
            }
            identityPath = configuration.getHostVendorImageFolder()
                    .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                    .concat(identityImage.getOriginalFilename()
                            .substring(identityImage.getOriginalFilename().indexOf(".")));
        }
        if (!licenseImage.isEmpty()) {
            if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(licenseImage.getContentType())) {
                String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
            }
            licensePath = configuration.getHostVendorImageFolder()
                    .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                    .concat(licenseImage.getOriginalFilename()
                            .substring(licenseImage.getOriginalFilename().indexOf(".")));
        }
        List<FileDataObject> addList = new ArrayList();
        if(!profileImage.isEmpty()) addList.add(new FileDataObject(profileImage, profilePath));
        if(!identityImage.isEmpty()) addList.add(new FileDataObject(identityImage, identityPath));
        if(!licenseImage.isEmpty()) addList.add(new FileDataObject(licenseImage, licensePath));
        Boolean saved = fileStorageService.save(addList);
        if(!saved){
            String imageNotUploadedMessage = messageSource.getMessage("error.not.uploaded.file", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotUploadedMessage);
        }

        Vendor vendorParams = Vendor.builder()
                .fullName(payload.getFullName())
                .vendorType(payload.getVendorType())
                .maxKilometerDelivery(payload.getMaxKilometerDelivery())
                .openingTime(payload.getOpeningTime())
                .closingTime(payload.getClosingTime())
                .minChargeLongDistance(payload.getMinChargeLongDistance())
                .user(user)
                .build();
        if(payload.getVendorId()!=null){
            vendorParams.setVendorId(payload.getVendorId());
            if(profilePath.contains("\\")) vendorParams.setProfileImage(Base64.getEncoder().encodeToString(profilePath.getBytes()));
            else vendorParams.setProfileImage(profilePath);
            if(identityPath.contains("\\")) vendorParams.setIdentityImage(Base64.getEncoder().encodeToString(identityPath.getBytes()));
            else vendorParams.setIdentityImage(identityPath);
            if(licensePath.contains("\\")) vendorParams.setLicenseImage(Base64.getEncoder().encodeToString(licensePath.getBytes()));
            else vendorParams.setLicenseImage(licensePath);
        } else {
            vendorParams.setProfileImage(Base64.getEncoder().encodeToString(profilePath.getBytes()));
            vendorParams.setIdentityImage(Base64.getEncoder().encodeToString(identityPath.getBytes()));
            vendorParams.setLicenseImage(Base64.getEncoder().encodeToString(licensePath.getBytes()));
        }
        Vendor createdVendor = vendorRepository.save(vendorParams);
        if(payload.getVendorId()==null)
            sequencesService.createSequenceFor("vendors", 1000, createdVendor.getVendorId());
        return createdVendor;
    }
    @Override
    public Boolean toggleWorkingById(Long vendorId) throws TabaldiGenericException, IOException {
        Vendor vendor = this.getVendorById(vendorId);
        int updated = vendorRepository.toggleWorkingById(!vendor.isWorking(), vendor.getVendorId());
        if(updated>0)
            return !vendor.isWorking();
        return vendor.isWorking();
    }
    @Override
    public Boolean deleteVendorById(Long vendorId) throws TabaldiGenericException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (!vendorOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Vendor", "التاجر");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Vendor vendor = vendorOptional.get();
            userService.deleteUserById(vendor.getUser().getUserId());
            List<String> list = List.of(vendor.getProfileImage(), vendor.getIdentityImage(), vendor.getLicenseImage());
            fileStorageService.remove(list.stream()
                    .map(path -> new String(Base64.getDecoder().decode(path.getBytes()))).collect(Collectors.toList()));
            return true;
        }
    }

    @Override
    public Vendor getVendorById(Long vendorId) throws TabaldiGenericException {
        Optional<Vendor> selectedVendor = vendorRepository.findById(vendorId);
        if(!selectedVendor.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Vendor","البائع");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedVendor.get();
    }

    @Override
    public Vendor getVendorByUserId(Long userId) throws TabaldiGenericException {
        Optional<Vendor> vendor = vendorRepository.findByUser(
                UserEntity.builder().userId(userId).build());
        if(!vendor.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Vendor","البائع");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return vendor.get();
    }
    @Override
    public Vendor getProfile() throws TabaldiGenericException {
        UserEntity myUserDetails = (UserEntity) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Session session = sessionService.getSessionByUsername(myUserDetails.getUsername());
        UserEntity user = session.getUser();
        return this.getVendorByUserId(user.getUserId());
    }

    @Override
    public List<Category> getVendorCategoriesList(Long vendorId) throws TabaldiGenericException {
        Vendor vendor = this.getVendorById(vendorId);
        List<Category> categoryList = categoryRepository.findByVendor(vendor);

        if(categoryList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"categories", "أنواع المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return categoryList;
    }
    @Override
    public List<Order> getVendorOrdersList(Long vendorId) throws TabaldiGenericException {
        Vendor vendor = this.getVendorById(vendorId);
        List<Order> orderList = orderService.getByVendor(vendor, true);

         orderService.fillOrdersDetails(orderList);
        return orderList;
    }

    @Override
    public List<Product> getVendorProductsList(Long vendorId) throws TabaldiGenericException, IOException {
        Vendor vendor = this.getVendorById(vendorId);
        List<Product> products = productRepository.findByVendor(vendor);
        if(products.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Product","المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (Product product : products) {
            product.setImages(GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class));
        }
        return products;
    }

    @Override
    public List<VendorFrequency> fetchFrequentVendorByOrders(List<Order> orders, int size) {
        List<VendorFrequency> vendorFrequency = orders.stream()
                .collect(Collectors.groupingBy(e -> e.getVendor(), Collectors.counting()))  // Step 1: Count frequency
                .entrySet()
                .stream()
                .map(entry -> new VendorFrequency(entry.getKey(), entry.getValue()))  // Step 2: Map to VendorFrequency
                .sorted((vf1, vf2) -> Long.compare(vf2.getFrequency(), vf1.getFrequency()))  // Step 3: Sort by frequency
                .limit(size)
                .collect(Collectors.toList());

        return vendorFrequency;
    }

    @Override
    public Long countAllProductsPerVendor(Long vendorId) {
        return productRepository.countByVendor(vendorId);
    }
}
