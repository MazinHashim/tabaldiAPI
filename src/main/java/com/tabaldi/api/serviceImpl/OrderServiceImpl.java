package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.InvoicePayload;
import com.tabaldi.api.payload.OrderPayload;
import com.tabaldi.api.payload.PendingOrders;
import com.tabaldi.api.repository.CartItemRepository;
import com.tabaldi.api.repository.OrderRepository;
import com.tabaldi.api.repository.ProductRepository;
import com.tabaldi.api.service.CustomerService;
import com.tabaldi.api.service.InvoiceService;
import com.tabaldi.api.service.OrderService;
import com.tabaldi.api.service.SequencesService;
import com.tabaldi.api.utils.GenericMapper;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SequencesService sequencesService;
    private final CustomerService customerService;
    private final InvoiceService invoiceService;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public List<Order> createAndSaveOrderInfo(long customerId, OrderPayload payload) throws TabaldiGenericException, IOException {

        Customer customer = customerService.getCustomerById(customerId);
        List<CartItem> cartItems = customerService.getCustomerActiveCartItemsList(customerId);
        if(cartItems.stream().anyMatch(cartItem ->
                !cartItem.getProduct().isPublished() || !cartItem.getProduct().getCategory().isPublished())) {
            String itemsInOrderNotAvailableMessage = messageSource.getMessage("error.items.in.order.not.available", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, itemsInOrderNotAvailableMessage);
        }
//        if user has only one active order, but for many orders should return list
        List<Vendor> vendors = cartItems.stream()
                .map(cartItem -> cartItem.getProduct().getVendor())
                .distinct().collect(Collectors.toList());
        LocalTime timeInUAE = LocalTime.ofInstant(Instant.now(), ZoneOffset.ofHours(4));
        if(vendors.stream().anyMatch(vendor -> !vendor.isWorking() ||
                !(timeInUAE.isBefore(vendor.getClosingTime())&&
                        timeInUAE.isAfter(vendor.getOpeningTime())))) {
            String itemsFromClosedVendorMessage = messageSource.getMessage("error.items.from.closed.vendor", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, itemsFromClosedVendorMessage);
        }
//        vendors.forEach((vendor -> System.out.println(vendor.getFullName())));
        boolean isAllNotRestaurant = vendors.stream()
                .allMatch(vendor -> !vendor.getVendorType().equals(VendorType.RESTAURANT));
        boolean isAllRestaurant = vendors.stream()
                .allMatch(vendor -> vendor.getVendorType().equals(VendorType.RESTAURANT));
        if(isAllNotRestaurant || isAllRestaurant){
            AtomicBoolean hasPendingVendorOrder= new AtomicBoolean(false);
            StringBuilder pending = new StringBuilder();
            vendors.forEach(vendor -> {
                Optional<Order> lastOrderCreatedOptional = orderRepository.getLastActiveOrderPerVendor(customerId, vendor.getVendorId());
                if(lastOrderCreatedOptional.isPresent()){
                    hasPendingVendorOrder.set(true);
                    pending.append(vendor.getFullName().concat(", "));
                }
            });
            if(hasPendingVendorOrder.get()){
                String pendingVendors = pending.substring(0, pending.lastIndexOf(", ")).trim();
                String pendingOrderMessage = MessagesUtils.getAlreadyHasPendingOrderMessage(messageSource,
                        pendingVendors, pendingVendors);
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, pendingOrderMessage);
            } else {
                List<Order> orders = new ArrayList<>(vendors.size());
                Address selectedAddress = customerService.getSelectedCustomerAddress(customerId);
                vendors.forEach(vendor -> {
                final String orderNumber = this.getOrderNumber(customer, vendor);
                Order orderParams = Order.builder()
                        .orderDate(OffsetDateTime.now())
                        .orderNumber(orderNumber)
                        .customer(customer)
                        .vendor(vendor)
                        .status(OrderStatus.WAITING)
                        .address(selectedAddress)
                        .build();
                    orders.add(orderParams);
                });
                List<Order> createdOrders = orderRepository.saveAll(orders);
                createdOrders.forEach(order -> {
                    // set cart items for each order related to vendor
                    order.setCartItems(cartItems.stream()
                            .filter(cartItem -> cartItem.getProduct().getVendor()==order.getVendor())
                            .collect(Collectors.toList()));

                    cartItems.forEach(cartItem -> {
                        // Check if the cart item belongs to the same vendor as the order
                        if(cartItem.getProduct().getVendor().getVendorId()==order.getVendor().getVendorId()) {
                            // Set the order for the cart item
                            cartItem.setOrder(order);
                            // Update product quantity
                            Product product = cartItem.getProduct();
                            int newQuantity = product.getQuantity() - cartItem.getQuantity();
                            product.setQuantity(newQuantity);
                            cartItemRepository.save(cartItem);

                            // Handle JSON to List conversion for images and options
                            try {
                                if (product.getImagesCollection() != null)
                                    product.setImages(GenericMapper
                                                    .jsonToListObjectMapper(cartItem.getProduct().getImagesCollection(), String.class));
                                if (cartItem.getOptionsCollection() != null)
                                    cartItem.setSelectedOptions(GenericMapper
                                            .jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            // Calculate the total price for the cart item
                            double itemTotal = cartItem.getPrice() * cartItem.getQuantity();
                            double itemTotalWithProfit = itemTotal+(itemTotal*cartItem.getProduct().getCompanyProfit()/100);
                            double roundedTotal = Math.round(itemTotalWithProfit*2)/2;
                            order.setTotal(order.getTotal() + roundedTotal);

                            // Add fees for selected options to the order total
                            cartItem.getSelectedOptions().forEach(option -> {
                                if (option.getFee() != null)
                                    order.setTotal(order.getTotal() + option.getFee());
                            });
                        }
                    });
                    if(payload.getPaymentMethod().equals(PaymentMethod.CASH) && order.getTotal()>70){
                        String onlyOneAllowedMessage = messageSource.getMessage("error.order.exceed.allowed.cash", null, LocaleContextHolder.getLocale());
                        try {
                            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, onlyOneAllowedMessage);
                        } catch (TabaldiGenericException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // Ensure the updated product quantities are saved
                    cartItems.stream()
                            .map(CartItem::getProduct)
                            .forEach(productRepository::save);

                    try {
                        double discount = payload.getDiscount()==null?0.0:payload.getDiscount();
                        double taxPercentage = order.getTotal() * payload.getTaxPercentage()/100;
                        Invoice createdInvoice = invoiceService.saveInvoiceInfo(InvoicePayload.builder()
                                .orderId(order.getOrderId())
                                .discount(discount)
                                .paymentMethod(payload.getPaymentMethod())
                                .shippingCost(payload.getShippingCost())
                                .taxes(taxPercentage)
                                .subtotal(order.getTotal())
                                .total(order.getTotal()+taxPercentage+discount+payload.getShippingCost())
                                .build(), order);
                        order.setTotal(createdInvoice.getSummary().getTotal());
                        if(!createdInvoice.getPaymentMethod().equals(PaymentMethod.CASH)){
                            invoiceService.payOrderInvoice(order.getOrderId(), payload.getCard());
                        }
                    } catch (TabaldiGenericException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                return createdOrders;
            }
        } else {
            String onlyOneAllowedMessage = messageSource.getMessage("error.separate.restaurant.order", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, onlyOneAllowedMessage);
        }
    }
    @Override
    public List<Order> getAllOrders(Long customerId) throws TabaldiGenericException {
        if(customerId==null) return orderRepository.findAll();
        else {
            Customer customer = customerService.getCustomerById(customerId);
            return orderRepository.findByCustomer(customer);
        }
    }

    @Override
    public Map<String, Long> countAllOrdersInSystem() {
        return Map.of("all", orderRepository.count(), "oneDay", orderRepository.countByOrderDate());
    }
    @Override
    public PendingOrders getPendingOrdersList(Long customerId) throws TabaldiGenericException{
        // fetch not delivered or canceled orders
        List<Order> ordersList;
        if(customerId!=null) {
            customerService.getCustomerById(customerId);
            ordersList = orderRepository.findPendingOrdersByCustomer(List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED), customerId);
        } else
            ordersList = orderRepository.findByPendingOrders(List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED));

        if(ordersList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Customer Orders", "طلبات الزبائن");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        this.fillOrdersDetails(ordersList);
        List<Order> pendingOrders = ordersList.stream()
                .map(order-> {
                    order.getCartItems().forEach(cartItem -> {
                        cartItem.getProduct().setOptions(null);
                        cartItem.setCustomer(null);
                    });
                    return order;
                })
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .collect(Collectors.toList());
        return PendingOrders.builder()
                .orders(pendingOrders)
                .count(pendingOrders.size())
                .build();
    }
    @Override
    public PendingOrders fetchPendingOrdersByVendor(List<Order> orders) {
        List<Order> activeOrders = orders.stream()
                .filter(order -> !order.getStatus().equals(OrderStatus.DELIVERED)
                        && !order.getStatus().equals(OrderStatus.CANCELED))
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .map(order -> {
                    order.getCartItems().forEach(cartItem -> {
                        cartItem.getProduct().setOptions(null);
                        cartItem.setCustomer(null);
                    });
                    return order;
                })
                .collect(Collectors.toList());
        return PendingOrders.builder()
                .orders(activeOrders)
                .count(activeOrders.size())
                .build();
    }

    @Override
    public List<Order> getByVendor(Vendor vendor, boolean check) throws TabaldiGenericException{
        List<Order> orderList = orderRepository.findByVendor(vendor);

        if(check && orderList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Customers Orders", "طلبات الزبائن'");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return orderList.stream().sorted(Comparator.comparing(Order::getOrderDate).reversed()).collect(Collectors.toList());
    }
    @Override
    public void fillOrderDetails(Order order) {
        order.getCartItems().forEach(cartItem -> {
            try {
                if(cartItem.getProduct().getImagesCollection()!=null)
                    cartItem.getProduct()
                            .setImages(GenericMapper
                                    .jsonToListObjectMapper(cartItem.getProduct().getImagesCollection(), String.class));
                if(cartItem.getOptionsCollection()!=null)
                    cartItem.setSelectedOptions(GenericMapper
                            .jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
                order.setTotal(invoiceService.getInvoiceByOrderId(order.getOrderId()).getSummary().getTotal());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (TabaldiGenericException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void fillOrdersDetails(List<Order> orderList) {
        orderList.forEach(order -> {
            this.fillOrderDetails(order);
        });
    }
    @Override
    public double fetchCompanyEarningsFromOrders(List<Order> orders) {
        AtomicReference<Double> companyEarnings= new AtomicReference<>((double) 0);
        orders.forEach(order -> {
            if(order.getStatus().equals(OrderStatus.DELIVERED)) {
                order.getCartItems().forEach(cartItem -> {
                    double companyEarningsPerItem = (cartItem.getPrice() * cartItem.getQuantity()) / 100 * cartItem.getProduct().getCompanyProfit();
                    companyEarnings.updateAndGet(v -> v + companyEarningsPerItem);
                });
            }
        });
        return companyEarnings.get();
    }

    @Override
    public double fetchVendorEarningsFromOrders(List<Order> orders) {
        AtomicReference<Double> vendorEarnings = new AtomicReference<>((double) 0);
        AtomicReference<Double> companyEarnings = new AtomicReference<>((double) 0);
        orders
                .stream().filter(order -> order.getOrderDate().getMonthValue()==5)//OffsetDateTime.now().getMonthValue())
                .forEach(order -> {
            if(order.getStatus().equals(OrderStatus.DELIVERED)) {
                order.getCartItems().forEach(cartItem -> {
                    double companyEarningsPerItem = (cartItem.getPrice() * cartItem.getQuantity()+10) / 100 * cartItem.getProduct().getCompanyProfit();
                    companyEarnings.updateAndGet(v -> v + companyEarningsPerItem);
                });
                vendorEarnings.updateAndGet(v -> order.getTotal() + v);
            }
        });
        return vendorEarnings.get()-companyEarnings.get();
    }

    @Override
    public Boolean changeOrderStatusById(Long orderId, OrderStatus status) throws TabaldiGenericException, IOException {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (!orderOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Order", "الطلب");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Order order = orderOptional.get();
            if(!isDeliveredOrCanceledAndThrown(order)) {
                if(status==null){
                    String notSupportedMessage = messageSource.getMessage("error.status.not.supported", null, LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notSupportedMessage);
                }
                if(status.equals(OrderStatus.CANCELED) && OffsetDateTime.now().minusMinutes(10).isAfter(order.getOrderDate())) {
                    String finalizedOrderMessage = messageSource.getMessage("error.finalized.order", null, LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, finalizedOrderMessage);
                }
                Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
                if( !status.equals(OrderStatus.WAITING) &&
                    !status.equals(OrderStatus.DELIVERED) &&
                    invoice.getStatus().equals(InvoiceStatus.UNPAID)){
                    String notPaidOrderMessage = messageSource.getMessage("error.invoice.not.paid", null, LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notPaidOrderMessage);
                }
                if(status.equals(OrderStatus.PROCESSING))
                    order.setProcessedDate(OffsetDateTime.now());
                else if(status.equals(OrderStatus.DELIVERED))
                    order.setDeliveredDate(OffsetDateTime.now());
                order.setStatus(status);
                orderRepository.save(order);
                 if(status.equals(OrderStatus.DELIVERED))
//                         || status.equals(OrderStatus.CONFIRMED))
                    invoiceService.payOrderInvoice(order.getOrderId(), null);
                return true;
            }
            return false;
        }
    }

    @Override
    public Order getOrderById(Long orderId) throws TabaldiGenericException {
        Optional<Order> selectedOrder = orderRepository.findById(orderId);
        if(!selectedOrder.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Order","الطلب");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedOrder.get();
    }

    @Override
    public List<CartItem> getOrderCartItemsList(Long orderId) throws TabaldiGenericException, IOException {
        Order order = this.getOrderById(orderId);
        List<CartItem> cartItems = cartItemRepository.findByOrder(order);
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

    private String getOrderNumber(Customer customer, Vendor vendor) {
        OffsetDateTime now = OffsetDateTime.now();
        long orderSequenceNumber = sequencesService.getNextSequenceFor("orders", null );
        long vendorSequenceNumber = sequencesService.getNextSequenceFor("vendors", vendor.getVendorId());
        long customerSequenceNumber = sequencesService.getNextSequenceFor("customers", customer.getCustomerId());
        String vendorPrefix = vendor.getFullName().substring(0, 2).toUpperCase();
        String customerPrefix = customer.getFirstName().substring(0, 2).toUpperCase();
        int dayNumberFromToday = now.getDayOfMonth();
        String orderNumber = String.format("%s%s%04d%04d%04d%02d", customerPrefix, vendorPrefix,
                vendorSequenceNumber, customerSequenceNumber, orderSequenceNumber, dayNumberFromToday);
        return orderNumber;
    }

    private boolean isDeliveredOrCanceledAndThrown(Order order) throws TabaldiGenericException {
        if(order.getStatus().equals(OrderStatus.CANCELED)){
            String orderStatus = OrderStatus.CANCELED.name().toLowerCase();
            String orderStatusMessage = MessagesUtils.getOrderStatusMessage(messageSource, orderStatus, orderStatus);
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, orderStatusMessage);
        } else if(order.getStatus().equals(OrderStatus.DELIVERED)){
            String orderStatus = OrderStatus.DELIVERED.name().toLowerCase();
            String orderStatusMessage = MessagesUtils.getOrderStatusMessage(messageSource, orderStatus, orderStatus);
            throw  new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, orderStatusMessage);
        } else {
            return false;
        }
    }
}
