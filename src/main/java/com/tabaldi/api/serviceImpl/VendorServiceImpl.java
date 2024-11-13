package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.*;
import com.tabaldi.api.repository.*;
import com.tabaldi.api.service.*;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import com.tabaldi.api.utils.RestUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

    private final MessageSource messageSource;

    private final SequencesService sequencesService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final AdvertisementRepository advertisementRepository;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final TabaldiConfiguration configuration;


    @Override
    public List<Vendor> getVendorsList(String roleName) throws TabaldiGenericException {

        List<Vendor> vendorList = roleName.equals(Role.SUPERADMIN.name())||roleName.equals(Role.VENDOR.name())
                ? vendorRepository.findAll()
                : vendorRepository.findByIsWorking(true);
        if(vendorList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"vendors", "البائعين");
            throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
        }

        vendorList.forEach(v-> {
            Long productCount = productRepository.countByIsPublishedAndVendor_vendorId(false, v.getVendorId());
            Long categoryCount = categoryRepository.countByIsPublishedAndVendor_vendorId(false, v.getVendorId());
            UserEntity user = userRepository.findByVendorAndRole(v, Role.VENDOR).stream().findFirst().get();
            v.setInactiveProductsCount(productCount.intValue());
            v.setInactiveCategoriesCount(categoryCount.intValue());
            v.setUserId(user.getUserId());
            v.setUserEmail(user.getEmail());
            v.setUserPhone(user.getPhone());
        });
        return vendorList.stream()
                .sorted(Comparator.comparing(Vendor::getFullName))
                .collect(Collectors.toList());
    }

    @Override
    public UserEntity addVendorUser(UserPayload payload) throws TabaldiGenericException {

        UserEntity user=null;
        if(payload.getUserId()!=null) {
            user = userService.getUserById(payload.getUserId());
        }
        UserEntity existEmail = userService.getExistByEmail(payload.getEmail());
        UserEntity existPhone = userService.getExistByPhone(payload.getPhone());

        Vendor selectedVendor=null;
        if(payload.getVendorId()!=null) {
            selectedVendor = this.getVendorById(payload.getVendorId());
        }
//        1/ check if entered phone and email are not exist
        if(existEmail==null && existPhone==null){
            user = UserEntity.builder()
                    .phone(payload.getPhone())
                    .email(payload.getEmail())
                    .agreeTermsConditions(payload.isAgreeTermsConditions())
                    .role(payload.getRole())
                    .build();
//            2/ assign both email and phone to updated user
            if(payload.getUserId()!=null){
                user.setUserId(payload.getUserId());
            }
            if(selectedVendor!=null){
                user.setVendor(selectedVendor);
            }
            user = userRepository.saveAndFlush(user);
//         3/ in update operation, if phone not exist or the exiting phone is belonged to updated user
        } else if(user!=null && (existPhone == null || existPhone.getUserId()==user.getUserId())) {
            user.setPhone(payload.getPhone());
//         4/ in update operation, if email not exist or the exiting email is belonged to updated user
        } else if(user!=null && (existEmail == null || existEmail.getUserId()==user.getUserId())){
            user.setEmail(payload.getEmail());
        } else {
//        5/ in add operation, if entered phone or email are exist
            if(user == null) {
                String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource, "phone", "رقم الهاتف");
                if (existEmail != null) {
                    alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource, "email", "البريد الإلكتروني");
                }
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
            }
        }
//        6/ in update operation, if email or phone are exist and aren't belonged to updated user
        if(user != null && existEmail!=null && existEmail.getUserId()!=user.getUserId()){
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"email", "البريد الإلكتروني");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        } else if (user != null && existPhone!=null && existPhone.getUserId()!=user.getUserId()) {
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"phone", "رقم الهاتف");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        }
        user = userRepository.saveAndFlush(user);
        return user;
    }
    @Override
    @Transactional
    public Vendor saveVendorInfo(VendorPayload payload, MultipartFile identityImage,
                                 MultipartFile licenseImage, MultipartFile profileImage, MultipartFile coverImage) throws TabaldiGenericException {
        String licensePath = "";
        String identityPath = "";
        String profilePath = "";
        String coverPath = "";
        boolean isWorking=false;
        UserEntity user;
        // update vendor constraints
        if (payload.getVendorId() != null) {
            Vendor vendor = this.getVendorById(payload.getVendorId());
//            UserEntity userByEmail = userService.getExistByEmail(payload.getEmail());
            licensePath = vendor.getLicenseImage()!=null?vendor.getLicenseImage():"";
            identityPath = vendor.getIdentityImage()!=null?vendor.getIdentityImage():"";
            profilePath = vendor.getProfileImage()!=null?vendor.getProfileImage():"";
            coverPath = vendor.getCoverImage()!=null?vendor.getCoverImage():"";
            isWorking = vendor.isWorking();
            user = userService.getUserById(payload.getUserId());
            if(!user.getPhone().equals(payload.getPhone()) || !user.getEmail().equals(payload.getEmail())) {
                user.setPhone(payload.getPhone());
                user.setEmail(payload.getEmail());
                userRepository.save(user);
            }
        } else {
            UserPayload userPayload = UserPayload.builder()
                    .email(payload.getEmail())
                    .phone(payload.getPhone())
                    .agreeTermsConditions(true)
                    .role(Role.VENDOR)
                    .build();
            user = this.addVendorUser(userPayload);
        }
        if(!user.getRole().equals(Role.VENDOR)){
            String mismatchMessage = MessagesUtils.getMismatchRoleMessage(messageSource, "Vendor","البائع");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, mismatchMessage);
        }
        if(payload.getVendorId() == null && (identityImage.isEmpty()||licenseImage.isEmpty())){
            String requiredImageUploadMessage = messageSource.getMessage("error.required.upload.file", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, requiredImageUploadMessage);
        }
        if (!profilePath.isEmpty()||!coverPath.isEmpty()||!identityPath.isEmpty()||!licensePath.isEmpty()) {
            List urlList = new ArrayList<String>();
            if(!profileImage.isEmpty() && !profilePath.isEmpty()) urlList.add(new String(Base64.getDecoder().decode(profilePath.getBytes())));
            if(!coverImage.isEmpty() && !coverPath.isEmpty()) urlList.add(new String(Base64.getDecoder().decode(coverPath.getBytes())));
            if(!identityImage.isEmpty() && !identityPath.isEmpty()) urlList.add(new String(Base64.getDecoder().decode(identityPath.getBytes())));
            if(!licenseImage.isEmpty() && !licensePath.isEmpty()) urlList.add(new String(Base64.getDecoder().decode(licensePath.getBytes())));
            fileStorageService.remove(urlList);
        }
        if (!profileImage.isEmpty()) {
            if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(profileImage.getContentType())) {
                String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
            }
            profilePath = configuration.getHostVendorImageFolder()
                    .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                    .concat(profileImage.getOriginalFilename()
                            .substring(profileImage.getOriginalFilename().indexOf(".")));
        }
        if (!coverImage.isEmpty()) {
            if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(coverImage.getContentType())) {
                String imageNotSupportedMessage = messageSource.getMessage("error.not.supported.file", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotSupportedMessage);
            }
            coverPath = configuration.getHostVendorImageFolder()
                    .concat(String.valueOf(OffsetDateTime.now().toEpochSecond()).concat(RandomString.make(10)))
                    .concat(coverImage.getOriginalFilename()
                            .substring(coverImage.getOriginalFilename().indexOf(".")));
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
        if(!coverImage.isEmpty()) addList.add(new FileDataObject(coverImage, coverPath));
        if(!identityImage.isEmpty()) addList.add(new FileDataObject(identityImage, identityPath));
        if(!licenseImage.isEmpty()) addList.add(new FileDataObject(licenseImage, licensePath));
        Boolean saved = fileStorageService.save(addList);
        if(!saved){
            String imageNotUploadedMessage = messageSource.getMessage("error.not.uploaded.file", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, imageNotUploadedMessage);
        }

        Vendor vendorParams = Vendor.builder()
                .fullName(payload.getFullName())
                .arFullName(payload.getArFullName())
                .vendorType(payload.getVendorType())
                .region(payload.getRegion())
                .lat(payload.getLat())
                .lng(payload.getLng())
                .maxKilometerDelivery(payload.getMaxKilometerDelivery())
                .openingTime(payload.getOpeningTime())
                .closingTime(payload.getClosingTime())
                .minChargeLongDistance(payload.getMinChargeLongDistance())
                .build();
        if(payload.getVendorId()!=null){
            vendorParams.setVendorId(payload.getVendorId());
            vendorParams.setWorking(isWorking);
            if(profilePath.contains(configuration.getHostVendorImageFolder())) vendorParams.setProfileImage(Base64.getEncoder().encodeToString(profilePath.getBytes()));
            else vendorParams.setProfileImage(profilePath);
            if(coverPath.contains(configuration.getHostVendorImageFolder())) vendorParams.setCoverImage(Base64.getEncoder().encodeToString(coverPath.getBytes()));
            else vendorParams.setCoverImage(coverPath);
            if(identityPath.contains(configuration.getHostVendorImageFolder())) vendorParams.setIdentityImage(Base64.getEncoder().encodeToString(identityPath.getBytes()));
            else vendorParams.setIdentityImage(identityPath);
            if(licensePath.contains(configuration.getHostVendorImageFolder())) vendorParams.setLicenseImage(Base64.getEncoder().encodeToString(licensePath.getBytes()));
            else vendorParams.setLicenseImage(licensePath);
        } else {
            vendorParams.setProfileImage(Base64.getEncoder().encodeToString(profilePath.getBytes()));
            vendorParams.setCoverImage(Base64.getEncoder().encodeToString(coverPath.getBytes()));
            vendorParams.setIdentityImage(Base64.getEncoder().encodeToString(identityPath.getBytes()));
            vendorParams.setLicenseImage(Base64.getEncoder().encodeToString(licensePath.getBytes()));
        }
        Vendor createdVendor = vendorRepository.save(vendorParams);
//        userRepository.findByVendorAndRole(createdVendor, Role.VENDOR)
        user.setVendor(createdVendor);
        userRepository.save(user);
//        createdVendor.setUserId(user.getUserId());
        if(payload.getVendorId()==null)
            sequencesService.createSequenceFor("vendors", 1000, createdVendor.getVendorId());
        return createdVendor;
    }
    @Override
    public Boolean toggleWorkingById(Long vendorId) throws TabaldiGenericException {
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
//            this.getVendorUsersList(vendor.getVendorId());
            vendorRepository.deleteById(vendor.getVendorId());
            List<String> list = List.of(vendor.getCoverImage() ,vendor.getProfileImage(), vendor.getIdentityImage(), vendor.getLicenseImage());
            fileStorageService.remove(list.stream()
                    .map(path -> new String(Base64.getDecoder().decode(path.getBytes()))).collect(Collectors.toList()));
            return true;
        }
    }
    @Override
    public Boolean deleteUserById(Long userId) throws TabaldiGenericException {
        UserEntity user = userService.getUserById(userId);
        if(!user.getRole().equals(Role.VENDOR_USER)) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "User","المستخدم");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
//        if (lastLogin is before lastLogout) => logged out
//        if (lastLogout is before lastLogin) => logged in
        return userService.deleteUserById(userId);
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
        // if you want to check role check it using PathVariable annotation
        // or in any case you should not access auth user here
        Optional<UserEntity> user = userRepository.findById(userId);
        if(!user.isPresent() || user.get().getVendor()==null){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Vendor","البائع");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return user.get().getVendor();
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
    public List<UserEntity> getVendorUsersList(Long vendorId) throws TabaldiGenericException {
        Vendor vendor = this.getVendorById(vendorId);
        List<UserEntity> userList = userRepository.findByVendorAndRole(vendor, Role.VENDOR_USER);

        if(userList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"User", "المستخدمين");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return userList;
    }
    @Override
    public List<Advertisement> getVendorAdvertisementsList(Long vendorId) throws TabaldiGenericException {
        Vendor vendor = this.getVendorById(vendorId);
        List<Advertisement> advertisementList = advertisementRepository.findByVendor(vendor);

        if(advertisementList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"advertisements", "الإعلانات");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return advertisementList.stream().sorted(Comparator.comparing(Advertisement::getPriority))
                .collect(Collectors.toList());
    }
    @Override
    public List<Order> getVendorOrdersList(Long vendorId) throws TabaldiGenericException, IOException {
        Vendor vendor = this.getVendorById(vendorId);
        List<Order> orderList = orderService.getByVendor(vendor, true);

         orderService.fillOrdersDetails(orderList);
        return orderList;
    }

    @Override
    public List<Product> getVendorProductsList(Long vendorId, String roleName) throws TabaldiGenericException, IOException {

        Vendor vendor = this.getVendorById(vendorId);
        List<Product> products = roleName.equals(Role.SUPERADMIN.name())||roleName.equals(Role.VENDOR.name())
                ? productRepository.findByVendor(vendor)
                : productRepository.findByVendorAndIsPublished(vendor, true);
        if(products.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Product","المنتج");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (Product product : products) {
            product.setImages(GenericMapper.jsonToListObjectMapper(product.getImagesCollection(), String.class));
        }
        return products.stream().sorted(Comparator.comparing(Product::getName).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorFrequency> fetchFrequentVendorByOrders(List<Order> orders, int size) {
        List<VendorFrequency> vendorFrequency = orders.stream()
                .map((order) -> {
                    Vendor v = order.getVendor();
                    UserEntity user = userRepository.findByVendorAndRole(v, Role.VENDOR).stream().findFirst().get();
                    v.setUserId(user.getUserId());
                    v.setUserEmail(user.getEmail());
                    v.setUserPhone(user.getPhone());
                    order.setVendor(v);
                    return order;
                })
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

    @Override
    public Map<String, Object> searchLocation(String query) throws TabaldiGenericException {
        Map<String, Object> result = RestUtils.getRequest("https://maps.googleapis.com/maps/api/place/autocomplete/json?input="
                        +query+"&components=country:AE&key=+"
                        +configuration.getGoogleCloudApiKey()+"&language=ar&radius=50000"
        ,null, Map.class, HttpServletResponse.SC_BAD_REQUEST, ""
        );

        return result;
    }
    @Override
    public Map<String, Object> getLocationDetails(String placeId) throws TabaldiGenericException {
        Map<String, Object> result = RestUtils.getRequest("https://maps.googleapis.com/maps/api/place/details/json?place_id="+placeId
                        +"&key="+configuration.getGoogleCloudApiKey()
                ,null, Map.class, HttpServletResponse.SC_BAD_REQUEST, "");
        return result;
    }
}
