package com.tabaldi.api.serviceImpl;

import com.ibm.icu.text.ArabicShapingException;
import com.tabaldi.api.TabaldiConfiguration;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.*;
import com.tabaldi.api.repository.CartItemRepository;
import com.tabaldi.api.repository.OrderRepository;
import com.tabaldi.api.repository.ProductRepository;
import com.tabaldi.api.service.*;
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
    private final NotificationService notificationService;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SequencesService sequencesService;
    private final CustomerService customerService;
    private final SessionService sessionService;
    private final PdfGeneratorService pdfGeneratorService;
    private final TabaldiConfiguration configuration;
    private final EmailService emailService;
    private final InvoiceService invoiceService;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public List<Order> createAndSaveOrderInfo(long customerId, OrderPayload payload)
            throws TabaldiGenericException, IOException, ArabicShapingException {

        Customer customer = customerService.getCustomerById(customerId);
        Session session = sessionService.getSessionByUsername(customer.getUser().getPhone());
        List<CartItem> cartItems = customerService.getCustomerActiveCartItemsList(customerId, true);

        // 1/ check if all cart items are published to create an order
        if (cartItems.stream().anyMatch(cartItem -> !cartItem.getProduct().isPublished()
                || !cartItem.getProduct().getCategory().isPublished())) {
            String itemsInOrderNotAvailableMessage = messageSource.getMessage("error.items.in.order.not.available",
                    null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, itemsInOrderNotAvailableMessage);
        }
        // 2/ check if all vendor are not out of service (working),
        // if one of them out of service should remove it items from the cart
        List<Vendor> vendors = cartItems.stream()
                .map(cartItem -> cartItem.getProduct().getVendor())
                .distinct().collect(Collectors.toList());
        LocalTime timeInUAE = LocalTime.ofInstant(Instant.now(), ZoneOffset.ofHours(4));
        if (vendors.stream().anyMatch(vendor -> !vendor.isWorking() ||
                !(timeInUAE.isBefore(vendor.getClosingTime()) &&
                        timeInUAE.isAfter(vendor.getOpeningTime())))) {
            String itemsFromClosedVendorMessage = messageSource.getMessage("error.items.from.closed.vendor", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, itemsFromClosedVendorMessage);
        }
        // 3/ check if all vendors are or are not of same restaurant type
        boolean isAllNotRestaurant = vendors.stream()
                .allMatch(vendor -> !vendor.getVendorType().equals(VendorType.RESTAURANT));
        boolean isAllRestaurant = vendors.stream()
                .allMatch(vendor -> vendor.getVendorType().equals(VendorType.RESTAURANT));
        if (isAllNotRestaurant || (isAllRestaurant && vendors.size() == 1)) {

            // 4/ check if there is any pending order for any of these vendors
            // AtomicBoolean hasPendingVendorOrder= new AtomicBoolean(false);
            // StringBuilder pending = new StringBuilder();
            // vendors.forEach(vendor -> {
            // Optional<Order> lastOrderCreatedOptional =
            // orderRepository.getLastActiveOrderPerVendor(customerId,
            // vendor.getVendorId());
            // if(lastOrderCreatedOptional.isPresent()){
            // hasPendingVendorOrder.set(true);
            // pending.append(vendor.getFullName().concat(", "));
            // }
            // });
            // if(hasPendingVendorOrder.get()){
            // String pendingVendors = pending.substring(0, pending.lastIndexOf(",
            // ")).trim();
            // String pendingOrderMessage =
            // MessagesUtils.getAlreadyHasPendingOrderMessage(messageSource,
            // pendingVendors, pendingVendors);
            // throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
            // pendingOrderMessage);
            // } else {

            // 5/ if all checks passed successfully, create an order for each vendor
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

            // 6/ assign created vendor order to same vendor cart items
            for (Order order : createdOrders) {// set cart items for each order related to vendor
                order.setCartItems(cartItems.stream()
                        .filter(cartItem -> cartItem.getProduct().getVendor() == order.getVendor())
                        .collect(Collectors.toList()));

                for (CartItem cartItem : cartItems) {// Check if the cart item belongs to the same vendor as the order
                    if (cartItem.getProduct().getVendor().getVendorId() == order.getVendor().getVendorId()) {
                        // Set the order for the cart item
                        cartItem.setOrder(order);
                        // 7/ Update product quantity by decreasing cart item quantity
                        Product product = cartItem.getProduct();
                        int newQuantity = product.getQuantity() - cartItem.getQuantity();
                        product.setQuantity(newQuantity);
                        cartItemRepository.save(cartItem);
                        // set images and options of product because it will be returned
                        if (product.getImagesCollection() != null)
                            product.setImages(GenericMapper
                                    .jsonToListObjectMapper(cartItem.getProduct().getImagesCollection(), String.class));
                        // set selected options of cart item because it will be added to the order total
                        if (cartItem.getOptionsCollection() != null)
                            cartItem.setSelectedOptions(GenericMapper
                                    .jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));

                        // 8/ Calculate the total price for the cart item and set order total
                        double itemTotal = cartItem.getPrice() * cartItem.getQuantity();
                        // double itemTotalWithProfit = itemTotal + (itemTotal *
                        // cartItem.getProduct().getCompanyProfit() / 100);
                        double roundedTotal = Math.round(itemTotal * 2) / 2;
                        order.setTotal(order.getTotal() + roundedTotal);

                        // Add fees for selected options to the order total
                        if (cartItem.getSelectedOptions() != null) {
                            cartItem.getSelectedOptions().forEach(option -> {
                                if (option.getFee() != null)
                                    order.setTotal(order.getTotal() + option.getFee());
                            });
                        }
                    }
                }
                // 8/ check if order's payment method is CASH and total is less than 70, the
                // order will be aborted
                if (payload.getPaymentMethod().equals(PaymentMethod.CASH) && order.getTotal() > 70) {
                    String onlyOneAllowedMessage = messageSource.getMessage("error.order.exceed.allowed.cash", null,
                            LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, onlyOneAllowedMessage);
                }
                // Ensure the updated product quantities are saved
                cartItems.stream()
                        .map(CartItem::getProduct)
                        .forEach(productRepository::save);
                // 9/ Create invoice for each created order
                double discount = payload.getDiscount() == null ? 0.0 : payload.getDiscount();
                double taxPercentage = order.getTotal() * payload.getTaxPercentage() / 100;
                Optional<ShippingCostPayload> shippingCostObj = payload.getShippingCosts().stream()
                        .filter(cost -> cost.getVendorId() == order.getVendor().getVendorId()).findFirst();
                if (!shippingCostObj.isPresent()) {
                    String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "vendor", "البائع");
                    throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
                }
                this.validateRestaurantDelivery(order, shippingCostObj.get().getDistance());
                double shippingCost = shippingCostObj.get().getShippingCost();
                Invoice createdInvoice = invoiceService.saveInvoiceInfo(InvoicePayload.builder()
                        .orderId(order.getOrderId())
                        .discount(discount)
                        .paymentMethod(payload.getPaymentMethod())
                        .shippingCost(shippingCost)
                        .taxes(taxPercentage)
                        .subtotal(order.getTotal())
                        .total(order.getTotal() + taxPercentage + discount + shippingCost)
                        .build(), order);
                order.setTotal(createdInvoice.getSummary().getTotal());
                order.setPaymentMethod(createdInvoice.getPaymentMethod());
                order.setShippingCost(createdInvoice.getSummary().getShippingCost());
                if (!createdInvoice.getPaymentMethod().equals(PaymentMethod.CASH)) {
                    createdInvoice = invoiceService.payOrderInvoice(order.getOrderId(), payload.getCard());
                }
                // send email with attached invoice to customer using email service
                if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                    pdfGeneratorService.generatePdf(createdInvoice, true);
                    emailService.sendEmailWithAttachment(
                            customer.getEmail(),
                            "Order Invoice of " + order.getVendor().getFullName(),
                            "Thanks for your order",
                            configuration.getInvoicePdfFolder() + "invoice_" + createdInvoice.getInvoiceNumber()
                                    + ".pdf");
                }
                // send push notification to customer using firebase service
                notificationService.sendPushNotificationByToken(NotificationPayload.builder()
                        .token(session.getDeviceToken())
                        .title("Rateena Order")
                        .body("Your order from " + order.getVendor().getFullName() + " has been created")
                        .build());
            }
            return createdOrders;
        } else {
            String onlyOneAllowedMessage = messageSource.getMessage("error.separate.restaurant.order", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, onlyOneAllowedMessage);
        }
    }

    @Override
    public List<Order> getAllOrders(Long customerId) throws TabaldiGenericException {
        if (customerId == null)
            return orderRepository.findAll();
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
    public PendingOrders getPendingOrdersList(Long customerId) throws TabaldiGenericException, IOException {
        // fetch not delivered or canceled orders
        List<Order> ordersList;
        if (customerId != null) {
            customerService.getCustomerById(customerId);
            ordersList = orderRepository
                    .findPendingOrdersByCustomer(List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED), customerId);
        } else
            ordersList = orderRepository.findByPendingOrders(List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED));

        if (ordersList.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Customer Orders",
                    "طلبات الزبائن");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        this.fillOrdersDetails(ordersList);
        List<Order> pendingOrders = ordersList.stream()
                .map(order -> {
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
    public List<Order> getByVendor(Vendor vendor, boolean check) throws TabaldiGenericException {
        List<Order> orderList = orderRepository.findByVendor(vendor);

        if (check && orderList.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Customers Orders",
                    "طلبات الزبائن'");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return orderList.stream().sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void fillOrderDetails(Order order) throws TabaldiGenericException, IOException {
        for (CartItem cartItem : order.getCartItems()) {
            if (cartItem.getProduct().getImagesCollection() != null)
                cartItem.getProduct()
                        .setImages(GenericMapper
                                .jsonToListObjectMapper(cartItem.getProduct().getImagesCollection(), String.class));
            if (cartItem.getOptionsCollection() != null)
                cartItem.setSelectedOptions(GenericMapper
                        .jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
            Invoice invoice = invoiceService.getInvoiceByOrderId(order.getOrderId());
            order.setTotal(invoice.getSummary().getTotal());
            order.setPaymentMethod(invoice.getPaymentMethod());
            order.setShippingCost(invoice.getSummary().getShippingCost());
        }
    }

    @Override
    public void fillOrdersDetails(List<Order> orderList) throws IOException, TabaldiGenericException {
        for (Order order : orderList) {
            this.fillOrderDetails(order);
        }
    }

    @Override
    public double fetchCompanyEarningsFromOrders(List<Order> orders) {
        AtomicReference<Double> companyEarnings = new AtomicReference<>((double) 0);
        orders.stream()
                .filter(order -> order.getOrderDate().toLocalDate().isEqual(OffsetDateTime.now().toLocalDate()))
                .filter(order -> order.getStatus().equals(OrderStatus.DELIVERED))
                .forEach(order -> {
                    order.getCartItems().forEach(cartItem -> {
                        double companyEarningsPerItem = (cartItem.getPrice() * cartItem.getQuantity()) / 100
                                * cartItem.getProduct().getCompanyProfit();
                        companyEarnings.updateAndGet(v -> v + companyEarningsPerItem);
                    });
                });
        return companyEarnings.get();
    }

    @Override
    public double fetchVendorEarningsFromOrders(List<Order> orders) {
        AtomicReference<Double> vendorEarnings = new AtomicReference<>((double) 0);
        AtomicReference<Double> companyEarnings = new AtomicReference<>((double) 0);
        orders
                .stream()
                .filter(order -> order.getOrderDate().toLocalDate().isEqual(OffsetDateTime.now().toLocalDate()))
                .filter(order -> order.getStatus().equals(OrderStatus.DELIVERED))
                .forEach(order -> {
                    order.getCartItems().forEach(cartItem -> {
                        double companyEarningsPerItem = (cartItem.getPrice() * cartItem.getQuantity() + 10) / 100
                                * cartItem.getProduct().getCompanyProfit();
                        companyEarnings.updateAndGet(v -> v + companyEarningsPerItem);
                    });
                    vendorEarnings.updateAndGet(v -> order.getTotal() + v);
                });
        return vendorEarnings.get() - companyEarnings.get();
    }

    @Override
    public Boolean changeOrderStatusById(Long orderId, OrderStatus status) throws TabaldiGenericException, IOException {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (!orderOptional.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Order", "الطلب");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Order order = orderOptional.get();
            if (!isDeliveredOrCanceledAndThrown(order)) {
                if (status == null) {
                    String notSupportedMessage = messageSource.getMessage("error.status.not.supported", null,
                            LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notSupportedMessage);
                }
                if (status.equals(OrderStatus.CANCELED)
                        && OffsetDateTime.now().minusMinutes(10).isAfter(order.getOrderDate())) {
                    String finalizedOrderMessage = messageSource.getMessage("error.finalized.order", null,
                            LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, finalizedOrderMessage);
                }
                Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
                if (!status.equals(OrderStatus.WAITING) &&
                        !status.equals(OrderStatus.DELIVERED) &&
                        invoice.getStatus().equals(InvoiceStatus.UNPAID)) {
                    String notPaidOrderMessage = messageSource.getMessage("error.invoice.not.paid", null,
                            LocaleContextHolder.getLocale());
                    throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notPaidOrderMessage);
                }
                if (status.equals(OrderStatus.PROCESSING))
                    order.setProcessedDate(OffsetDateTime.now());
                else if (status.equals(OrderStatus.DELIVERED))
                    order.setDeliveredDate(OffsetDateTime.now());
                order.setStatus(status);
                orderRepository.save(order);
                if (status.equals(OrderStatus.DELIVERED))
                    // || status.equals(OrderStatus.CONFIRMED))
                    invoiceService.payOrderInvoice(order.getOrderId(), null);
                return true;
            }
            return false;
        }
    }

    @Override
    public Order getOrderById(Long orderId) throws TabaldiGenericException {
        Optional<Order> selectedOrder = orderRepository.findById(orderId);
        if (!selectedOrder.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Order", "الطلب");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return selectedOrder.get();
    }

    @Override
    public List<CartItem> getOrderCartItemsList(Long orderId) throws TabaldiGenericException, IOException {
        Order order = this.getOrderById(orderId);
        List<CartItem> cartItems = cartItemRepository.findByOrder(order);
        if (cartItems.isEmpty()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Cart Items", "أغراض السلة");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        for (CartItem cartItem : cartItems.stream()
                .filter(cartItem -> cartItem.getOptionsCollection() != null)
                .collect(Collectors.toList()))
            cartItem.setSelectedOptions(
                    GenericMapper.jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));
        return cartItems;
    }

    private String getOrderNumber(Customer customer, Vendor vendor) {
        OffsetDateTime now = OffsetDateTime.now();
        long orderSequenceNumber = sequencesService.getNextSequenceFor("orders", null);
        long vendorSequenceNumber = sequencesService.getNextSequenceFor("vendors", vendor.getVendorId());
        long customerSequenceNumber = sequencesService.getNextSequenceFor("customers", customer.getCustomerId());
        String vendorPrefix = vendor.getFullName().length() < 2 ? ""
                : vendor.getFullName().substring(0, 2).toUpperCase();
        String customerPrefix = customer.getEmail() == null || customer.getEmail().length() < 2 ? ""
                : customer.getEmail().substring(0, 2).toUpperCase();
        int dayNumberFromToday = now.getDayOfMonth();
        String orderNumber = String.format("%s%s%04d%04d%04d%02d", customerPrefix, vendorPrefix,
                vendorSequenceNumber, customerSequenceNumber, orderSequenceNumber, dayNumberFromToday);
        return orderNumber;
    }

    private boolean isDeliveredOrCanceledAndThrown(Order order) throws TabaldiGenericException {
        if (order.getStatus().equals(OrderStatus.CANCELED)) {
            String orderStatus = OrderStatus.CANCELED.name().toLowerCase();
            String orderStatusMessage = MessagesUtils.getOrderStatusMessage(messageSource, orderStatus, orderStatus);
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, orderStatusMessage);
        } else if (order.getStatus().equals(OrderStatus.DELIVERED)) {
            String orderStatus = OrderStatus.DELIVERED.name().toLowerCase();
            String orderStatusMessage = MessagesUtils.getOrderStatusMessage(messageSource, orderStatus, orderStatus);
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, orderStatusMessage);
        } else {
            return false;
        }
    }

    private void validateRestaurantDelivery(Order order, double orderDistance)
            throws TabaldiGenericException {
        Vendor vendor = order.getVendor();
        if (vendor.getVendorType() != VendorType.RESTAURANT || vendor.getMaxKilometerDelivery() == null) {
            return; // Not applicable for non-restaurant vendors or those without max delivery
                    // distance
        }

        if (orderDistance > vendor.getMaxKilometerDelivery() &&
                order.getTotal() > vendor.getMinChargeLongDistance()) {

            String errorMessage = MessagesUtils.getOrderExceededScopeMessage(messageSource,
                    vendor.getMinChargeLongDistance().toString(), vendor.getMinChargeLongDistance().toString());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
        }
    }
}
