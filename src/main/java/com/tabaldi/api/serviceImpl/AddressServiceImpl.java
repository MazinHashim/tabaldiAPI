package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.model.Address;
import com.tabaldi.api.payload.AddressPayload;
import com.tabaldi.api.repository.AddressRepository;
import com.tabaldi.api.service.AddressService;
import com.tabaldi.api.service.CustomerService;
import com.tabaldi.api.service.SessionService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerService customerService;
    private final MessageSource messageSource;

    @Override
    public Address getAddressById(Long addressId) throws TabaldiGenericException {
        Optional<Address> selectedAddress = addressRepository.findById(addressId);
        if(!selectedAddress.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Address","العنوان");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedAddress.get();
    }

    @Override
    public Address saveAddressInfo(AddressPayload payload) throws TabaldiGenericException {
        // update address constraints
        boolean isSelected = false;
        if(payload.getAddressId()!=null){
            Address address = this.getAddressById(payload.getAddressId());
            isSelected = address.isSelected();
            if(address.getCustomer()!=null && address.getCustomer().getCustomerId()!=payload.getCustomerId()){
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource,"Address", "العنوان");
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
            }
//            else if(payload.getLatitude()!=address.getLatitude() || payload.getLongitude()!=address.getLongitude()){
//                String changeCoordinatesMessage = messageSource.getMessage("error.change.coordinates", null, LocaleContextHolder.getLocale());
//                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeCoordinatesMessage);
//            }
        }
        Customer selectedCustomer = customerService.getCustomerById(payload.getCustomerId());
        if (payload.getAddressId() == null && this.checkIfLatLngExistsPerCustomer(selectedCustomer,
                                                payload.getLatitude(), payload.getLongitude())) {
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource,"Address", "العنوان");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, "إحداثيات "+alreadyExistMessage);
        } else {
            Address addressParams = Address.builder()
                    .name(payload.getName())
                    .street(payload.getStreet())
                    .region(payload.getRegion())
                    .phone(payload.getPhone())
                    .latitude(payload.getLatitude())
                    .longitude(payload.getLongitude())
                    .customer(selectedCustomer)
                    .build();
            if(payload.getAddressId() == null) {
                addressParams.setSelected(true);
                Optional<Address> lastSelected = addressRepository.findBySelectedAndCustomer(true, selectedCustomer);
                if(lastSelected.isPresent()){
                    addressRepository.changeAddressSelection(false, lastSelected.get().getCustomer(),
                                                                     lastSelected.get().getAddressId());
                }
            }
            if (payload.getAddressId() != null)
                addressParams.setAddressId(payload.getAddressId());
            return addressRepository.save(addressParams);
        }
    }

    @Override
    public Boolean deleteAddressById(Long addressId) throws TabaldiGenericException {
        Optional<Address> addressOptional = addressRepository.findById(addressId);
        if (!addressOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Address", "العنوان");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Address address = addressOptional.get();
            if(address.isSelected()){
                String notDeletedMessage = messageSource.getMessage("error.selected.deletion", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notDeletedMessage);
            }
            addressRepository.deleteById(address.getAddressId());
            return true;
        }
    }
    @Override
    public Address changeSelectedAddress(Long addressId) throws TabaldiGenericException {
        Optional<Address> addressOptional = addressRepository.findById(addressId);
        if (!addressOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Address", "العنوان");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Address address = addressOptional.get();
            Optional<Address> selected = addressRepository.findBySelectedAndCustomer(true, address.getCustomer());
            if(!selected.isPresent()) {
                address.setSelected(true);
                addressRepository.changeAddressSelection(true, address.getCustomer(), address.getAddressId());
            } else {
                if(selected.get().getAddressId()==address.getAddressId()) {
                    String alreadySelectedMessage = MessagesUtils.getAlreadySelectedMessage(messageSource,"Address", "العنوان");
                    throw  new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadySelectedMessage);
                } else {
                    addressRepository.changeAddressSelection(false, selected.get().getCustomer(), selected.get().getAddressId());
                    addressRepository.changeAddressSelection(true, address.getCustomer(), address.getAddressId());
                    address.setSelected(true);
                }
            }
            return address;
        }
    }

    private boolean checkIfLatLngExistsPerCustomer(Customer selectedCustomer, double lat,double lng) {
        return addressRepository.existsByLatLongPerCustomer(selectedCustomer, lat, lng) != 0;
    }
}
