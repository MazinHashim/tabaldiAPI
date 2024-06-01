package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.CustomerFrequency;
import com.tabaldi.api.payload.CustomerPayload;
import com.tabaldi.api.repository.AddressRepository;
import com.tabaldi.api.repository.CartItemRepository;
import com.tabaldi.api.repository.CustomerRepository;
import com.tabaldi.api.repository.OrderRepository;
import com.tabaldi.api.service.CustomerService;
import com.tabaldi.api.service.SequencesService;
import com.tabaldi.api.service.SessionService;
import com.tabaldi.api.service.UserService;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final SequencesService sequencesService;
    private final SessionService sessionService;
    private final UserService userService;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public Customer saveCustomerInfo(CustomerPayload payload) throws TabaldiGenericException {
        // update customer constraints
        if(payload.getCustomerId()!=null){
            Customer customer = this.getCustomerById(payload.getCustomerId());
            if(customer.getUser()!=null &&
                    customer.getUser().getUserId()!=payload.getUserId()){
                String changeNotAllowedMessage = MessagesUtils.getNotChangeUserMessage(messageSource,"Customer", "الزبون");
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, changeNotAllowedMessage);
            }
        }
        UserEntity user = userService.getUserById(payload.getUserId());
        if(!user.getRole().equals(Role.CUSTOMER)){
            String mismatchMessage = MessagesUtils.getMismatchRoleMessage(messageSource, "Customer","الزبون");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, mismatchMessage);
        }
        if(payload.getCustomerId()==null && userService.checkUserExistRegardlessOfRole(user)){
            String alreadyExistMessage = MessagesUtils.getAlreadyExistMessage(messageSource, "Customer", "الزبون");
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, alreadyExistMessage);
        }
        Customer customerParams = Customer.builder()
                .firstName(payload.getFirstName())
                .lastName(payload.getLastName())
                .gender(payload.getGender())
                .dateOfBirth(payload.getDateOfBirth())
                .user(user)
                .build();
        if(payload.getCustomerId()!=null)
            customerParams.setCustomerId(payload.getCustomerId());
        Customer createdCustomer = customerRepository.save(customerParams);
        if(payload.getCustomerId()==null)
            sequencesService.createSequenceFor("customers", 1000, createdCustomer.getCustomerId());
        return createdCustomer;
    }

    @Override
    public Boolean deleteCustomerById(Long customerId) throws TabaldiGenericException {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (!customerOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Customer", "الزبون");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Customer customer = customerOptional.get();
            userService.deleteUserById(customer.getUser().getUserId());
            return true;
        }
    }

    @Override
    public Customer getCustomerById(Long customerId) throws TabaldiGenericException {
        Optional<Customer> selectedCustomer = customerRepository.findById(customerId);
        if(!selectedCustomer.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Customer","الزبون");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedCustomer.get();
    }

    @Override
    public Customer getCustomerByUserId(Long userId) throws TabaldiGenericException {
        Optional<Customer> customer = customerRepository.findByUser(
                UserEntity.builder().userId(userId).build());
        if(!customer.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Customer","الزبون");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return customer.get();
    }

    @Override
    public Address getSelectedCustomerAddress(Long customerId) throws TabaldiGenericException {
        Optional<Address> address = addressRepository.findBySelectedAndCustomer(
                true, Customer.builder().customerId(customerId).build());
        if(!address.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Address","العنوان");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return address.get();
    }
    @Override
    public Customer getProfile() throws TabaldiGenericException {
        UserEntity myUserDetails = (UserEntity) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Session session = sessionService.getSessionByUsername(myUserDetails.getUsername());
        UserEntity user = session.getUser();
        return this.getCustomerByUserId(user.getUserId());
    }

    @Override
    public List<CartItem> getCustomerCartItemsList(Long customerId) throws TabaldiGenericException, IOException {
        Customer customer = this.getCustomerById(customerId);
        List<CartItem> cartItems = cartItemRepository.findByCustomer(customer);
        if(cartItems.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Items","أغراض السلة");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (CartItem cartItem : cartItems.stream()
                .filter(cartItem -> cartItem.getOptionsCollection()!=null)
                .collect(Collectors.toList()))
            cartItem.setSelectedOptions(GenericMapper.jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
        return cartItems;
    }
    @Override
    public List<CartItem> getCustomerActiveCartItemsList(Long customerId) throws TabaldiGenericException, IOException {
        Customer customer = this.getCustomerById(customerId);
        List<CartItem> cartItems = cartItemRepository.findByCustomerAndOrderIsNull(customer);
        if(cartItems.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Items","أغراض السلة");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (CartItem cartItem : cartItems.stream()
                .filter(cartItem -> cartItem.getOptionsCollection()!=null)
                .collect(Collectors.toList()))
            cartItem.setSelectedOptions(GenericMapper.jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
        return cartItems;
    }
    @Override
    public List<Order> getCustomerOrdersList(Long customerId) throws TabaldiGenericException {
        Customer customer = this.getCustomerById(customerId);
        List<Order> orders = orderRepository.findByCustomer(customer);
        if(orders.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Orders","الطلبات");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return orders;
    }
    @Override
    public List<Address> getCustomerAddressesList(Long customerId) throws TabaldiGenericException {
        Customer customer = this.getCustomerById(customerId);
        List<Address> addressList = addressRepository.findByCustomer(customer);
        if(addressList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"addresses", "العناوين");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return addressList;
    }

    @Override
    public Map<String, Long> countAllCustomerInSystem() {
        OffsetDateTime endDate = OffsetDateTime.now();
        OffsetDateTime startDate = endDate.minusDays(2);
        return Map.of("all", customerRepository.count(),
                "twoDays", customerRepository.countByCustomerDate(startDate, endDate));
    }


    @Override
    public List<CustomerFrequency> fetchFrequentCustomerByOrders(List<Order> orders, int size) {

        List<CustomerFrequency> customerFrequencies = orders.stream()
                .collect(Collectors.groupingBy(Order::getCustomer, Collectors.counting()))  // Step 1: Count frequency
                .entrySet()
                .stream()
                .map(entry -> new CustomerFrequency(entry.getKey(), entry.getValue()))  // Step 2: Map to CustomerFrequency
                .sorted((cf1, cf2) -> Long.compare(cf2.getFrequency(), cf1.getFrequency()))  // Step 3: Sort by frequency
                .limit(size)
                .collect(Collectors.toList());

        return customerFrequencies;
    }
}
