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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.tabaldi.api.model.OrderStatus.*;

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
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    @Transactional(rollbackFor = {TabaldiGenericException.class, IOException.class, HttpClientErrorException.class, ArabicShapingException.class})
    public List<Order> createAndSaveOrderInfo(long customerId, OrderPayload payload)
            throws TabaldiGenericException, IOException, ArabicShapingException, HttpClientErrorException {

        Customer customer = customerService.getCustomerById(customerId);
        Session session = sessionService.getSessionByUsername(customer.getUser().getPhone());
        List<CartItem> cartItems = customerService.getCustomerActiveCartItemsList(customerId, true);

        List<Vendor> vendors = cartItems.stream()
                .map(cartItem -> cartItem.getProduct().getVendor())
                .distinct().collect(Collectors.toList());
        boolean isWillPass = this.checkIfOrderWillPass(customerId, payload.getShippingCosts());
        // 5/ if all checks passed successfully, create an order for each vendor
        List<Order> orders = new ArrayList<>(vendors.size());
        Address selectedAddress = customerService.getSelectedCustomerAddress(customerId);
        for (Vendor vendor : vendors) {
            final String orderNumber = this.getOrderNumber(customer, vendor);
            Order orderParams = Order.builder()
                    .orderDate(OffsetDateTime.now())
                    .orderNumber(orderNumber)
                    .customer(customer)
                    .vendor(vendor)
                    .status(WAITING)
                    // TODO: Require Review
//                    .addressObject(GenericMapper.objectToJSONMapper(selectedAddress))
                    .address(selectedAddress)
                    .build();
            if(payload.getComment()!=null && !payload.getComment().isEmpty())
                orderParams.setComment(payload.getComment());
            orders.add(orderParams);
        }

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
                    double roundedTotal = Math.round(itemTotal * 2) / 2;
                    order.setTotal(order.getTotal() + roundedTotal);

                    // Add fees for selected options to the order total
                    if (cartItem.getSelectedOptions() != null) {
                        cartItem.getSelectedOptions().forEach(option -> {
                            if (option.getFee() != null)
                                order.setTotal(order.getTotal() + (option.getFee() * cartItem.getQuantity()));
                        });
                    }
                }
            }
            // 8/ check if order's payment method is CASH and total is greater than 70, the
            // order will be aborted
//            if (payload.getPaymentMethod().equals(PaymentMethod.CASH) && order.getTotal() > 70) {
//                String onlyOneAllowedMessage = messageSource.getMessage("error.order.exceed.allowed.cash", null,
//                        LocaleContextHolder.getLocale());
//                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, onlyOneAllowedMessage);
//            }
            // Ensure the updated product quantities are saved
            cartItems.stream()
                    .map(CartItem::getProduct)
                    .forEach(productRepository::save);
            // 9/ Create invoice for each created order
            double discount = payload.getDiscount() == null ? 0.0 : payload.getDiscount();
            double taxPercentage = 0.0;// order.getTotal() * payload.getTaxPercentage() / 100;
            Optional<ShippingCostPayload> shippingCostObj = payload.getShippingCosts().stream()
                    .filter(cost -> cost.getVendorId() == order.getVendor().getVendorId()).findFirst();
            if (!shippingCostObj.isPresent()) {
                String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "vendor", "البائع");
                throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
            }
            this.validateDeliveryDistance(order.getVendor(), order.getTotal(), shippingCostObj.get().getDistance());
            double shippingCost = shippingCostObj.get().getShippingCost();
            Invoice createdInvoice = invoiceService.saveInvoiceInfo(InvoicePayload.builder()
                    .orderId(order.getOrderId())
                    .discount(discount)
                    .paymentMethod(payload.getPaymentMethod())
                    .shippingCost(shippingCost)
                    .taxes(taxPercentage)
                    .subtotal(order.getTotal())
                    .total(order.getTotal() + discount + taxPercentage + shippingCost)
                    .build(), order);
            order.setTotal(createdInvoice.getSummary().getTotal());
            order.setPaymentMethod(createdInvoice.getPaymentMethod());
            order.setShippingCost(createdInvoice.getSummary().getShippingCost());
            if (createdInvoice.getPaymentMethod().equals(PaymentMethod.MASTER_CARD)
                    || createdInvoice.getPaymentMethod().equals(PaymentMethod.VISA)) {
                createdInvoice = invoiceService.payOrderInvoice(order.getOrderId(), payload.getCard());
            }
            // send email with attached invoice to customer using email service
            if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                pdfGeneratorService.generatePdf(createdInvoice, true);
                String invoiceEmailSubject = MessagesUtils.getInvoiceEmailSupject(messageSource,
                        order.getVendor().getFullName(), !order.getVendor().getArFullName().isEmpty()
                                ? order.getVendor().getArFullName() : order.getVendor().getFullName());
                String invoiceEmailBody = messageSource.getMessage("success.invoice.email.body",
                        null, LocaleContextHolder.getLocale());
                emailService.sendEmailWithAttachment(
                        customer.getEmail(),
                        invoiceEmailSubject ,
                        invoiceEmailBody,
                        configuration.getInvoicePdfFolder() + "invoice_" + createdInvoice.getInvoiceNumber()
                                + ".pdf");
            }
            // send push notification to customer using firebase service
            String notificationTitle = messageSource.getMessage("success.notification.title",
                    null, LocaleContextHolder.getLocale());
            String notificationBody = MessagesUtils.getOrderNotificationBody(messageSource,
                    order.getVendor().getFullName(), !order.getVendor().getArFullName().isEmpty()
                            ? order.getVendor().getArFullName() : order.getVendor().getFullName(),
                    "created", "إنشاء");
            notificationService.sendPushNotificationByToken(NotificationPayload.builder()
                    .token(session.getDeviceToken())
                    .title(notificationTitle +" "+ order.getOrderNumber())
                    .body(notificationBody)
                    .build());
        }
        return createdOrders;
    }

    @Override
    public Boolean checkIfOrderWillPass(Long customerId, List<ShippingCostPayload> payload)
            throws TabaldiGenericException, IOException {
        customerService.getCustomerById(customerId);
        List<CartItem> cartItems = customerService.getCustomerActiveCartItemsList(customerId, true);

        // 1/ check if all cart items has published products and categories to create an
        // order
        if (cartItems.stream().anyMatch(cartItem -> !cartItem.getProduct().isPublished()
                || !cartItem.getProduct().getCategory().isPublished())) {
            String itemsInOrderNotAvailableMessage = messageSource.getMessage("error.items.in.order.not.available",
                    null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, itemsInOrderNotAvailableMessage);
        }
        // 2/ check if all vendor are not out of service (working) and restaurant is
        // opened,
        // if one of them out of service needs to remove it items from the cart
        List<Vendor> vendors = cartItems.stream()
                .map(cartItem -> cartItem.getProduct().getVendor())
                .distinct().collect(Collectors.toList());

        if (vendors.stream().anyMatch(vendor -> !vendor.isWorking() || vendor.getVendorType().equals("RESTAURANT")
                && !vendor.isStillOpening())) {
            String itemsFromClosedVendorMessage = messageSource.getMessage("error.items.from.closed.vendor", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, itemsFromClosedVendorMessage);
        }
        // 3/ check if all vendors are or are not of same restaurant type
        boolean isAllNotRestaurant = vendors.stream()
                .allMatch(vendor -> !vendor.getVendorType().equals(VendorType.RESTAURANT));
        boolean isAllRestaurant = vendors.stream()
                .allMatch(vendor -> vendor.getVendorType().equals(VendorType.RESTAURANT));
        if (!(isAllNotRestaurant || (isAllRestaurant && vendors.size() == 1))) {
            String onlyOneAllowedMessage = messageSource.getMessage("error.separate.restaurant.order", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, onlyOneAllowedMessage);
        }
        // // 4/ check if there is any pending order for any of these restaurant vendors
        // AtomicBoolean hasPendingVendorOrder = new AtomicBoolean(false);
        // StringBuilder pending = new StringBuilder();
        // vendors.stream().filter(vendor->
        // vendor.getVendorType().equals(VendorType.RESTAURANT))
        // .forEach(vendor -> {
        // List<Order> lastOrderCreatedOptional =
        // orderRepository.getLastActiveOrderPerVendor(customerId,
        // vendor.getVendorId());
        // if (!lastOrderCreatedOptional.isEmpty()) {
        // hasPendingVendorOrder.set(true);
        // pending.append(vendor.getFullName().concat(", "));
        // }
        // });
        // if (hasPendingVendorOrder.get()) {
        // String pendingVendors = pending.substring(0, pending.lastIndexOf(",
        // ")).trim();
        // String pendingOrderMessage =
        // MessagesUtils.getAlreadyHasPendingOrderMessage(messageSource,
        // pendingVendors, pendingVendors);
        // throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST,
        // pendingOrderMessage);
        // }
        for (Vendor vendor : vendors) {
            double orderTotal = 0;
            for (CartItem cartItem : cartItems) {
                // Check if the cart item belongs to the same vendor as the order
                if (cartItem.getProduct().getVendor().getVendorId() == vendor.getVendorId()) {
                    // set selected options of cart item because it will be added to the order total
                    if (cartItem.getOptionsCollection() != null)
                        cartItem.setSelectedOptions(GenericMapper
                                .jsonToListObjectMapper(cartItem.getOptionsCollection(), Option.class));

                    // Calculate the total price for the cart item and set order total
                    double itemTotal = cartItem.getPrice() * cartItem.getQuantity();
                    double roundedTotal = Math.round(itemTotal * 2) / 2;
                    orderTotal += roundedTotal;

                    // Add fees for selected options to the order total
                    if (cartItem.getSelectedOptions() != null) {
                        for (Option option : cartItem.getSelectedOptions()) {
                            if (option.getFee() != null)
                                orderTotal += (option.getFee() * cartItem.getQuantity());
                        }
                    }
                }
            }
            // 5/ Check Restaurant Delivery Max Distances Allowed And Minimum Charges
            Optional<ShippingCostPayload> shippingCostObj = payload.stream()
                    .filter(cost -> cost.getVendorId() == vendor.getVendorId()).findFirst();
            if (!shippingCostObj.isPresent()) {
                String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Shipping Cost", "رسوم التوصيل");
                throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
            }
            this.validateDeliveryDistance(vendor, orderTotal, shippingCostObj.get().getDistance());
        }
        return true;
    }

    @Override
    public List<Order> getAllOrders(Long customerId) throws TabaldiGenericException {
        if (customerId == null)
            return orderRepository.findAllHasInvoice();
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
                    .findPendingOrdersByCustomer(List.of(DELIVERED, CANCELED), customerId);
        } else
            ordersList = orderRepository.findByPendingOrders(List.of(DELIVERED, CANCELED));

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
                .filter(order -> !order.getStatus().equals(DELIVERED)
                        && !order.getStatus().equals(CANCELED))
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
            // TODO: Require Review
//            order.setAddress(GenericMapper.jsonToObjectMapper(order.getAddressObject(), Address.class));
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
                .filter(order -> order.getStatus().equals(DELIVERED))
                .forEach(order -> {
                    order.getCartItems().forEach(cartItem -> {
                        double companyEarningsPerItem = (cartItem.getProduct().getFinalPrice()
                                - cartItem.getProduct().getPrice()) * cartItem.getQuantity();
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
            .filter(order -> order.getStatus().equals(DELIVERED))
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
    public String saveVendorNote(Long orderId, String vendorNote) throws TabaldiGenericException {
        Order order = this.getOrderById(orderId);
        if (order.getStatus().equals(DELIVERED) || order.getStatus().equals(CANCELED)) {
            String finalizedOrderMessage = messageSource.getMessage("error.finalized.order", null,
                    LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, finalizedOrderMessage);
        }
        order.setVendorNotes(vendorNote);
        orderRepository.save(order);
        return vendorNote;
    }

    @Override
    public Boolean changeOrderStatusById(Long orderId, OrderStatus status) throws TabaldiGenericException, IOException {
        Order order = this.getOrderById(orderId);
        if (!isDeliveredOrCanceledAndThrown(order)) {
            if (status == null) {
                String notSupportedMessage = messageSource.getMessage("error.status.not.supported", null,
                        LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notSupportedMessage);
            }
            if (status.equals(CANCELED)
                    && OffsetDateTime.now().minusMinutes(10).isAfter(order.getOrderDate())) {
                String finalizedOrderMessage = messageSource.getMessage("error.finalized.order", null,
                        LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, finalizedOrderMessage);
            }
            Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
            if (!status.equals(WAITING) &&
                    !status.equals(DELIVERED) &&
                    !invoice.getPaymentMethod().equals(PaymentMethod.CASH) &&
                    invoice.getStatus().equals(InvoiceStatus.UNPAID)) {
                String notPaidOrderMessage = messageSource.getMessage("error.invoice.not.paid", null,
                        LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notPaidOrderMessage);
            }
            if (status.equals(PROCESSING))
                order.setProcessedDate(OffsetDateTime.now());
            else if (status.equals(DELIVERED))
                order.setDeliveredDate(OffsetDateTime.now());
            order.setStatus(status);
            orderRepository.save(order);
            // send push notification to customer using firebase service
            Session session = sessionService.getSessionByUsername(order.getCustomer().getUser().getPhone());
            String notificationTitle = messageSource.getMessage("success.notification.title",
                    null, LocaleContextHolder.getLocale());
            String notificationBody = MessagesUtils.getOrderNotificationBody(messageSource,
                    order.getVendor().getFullName(), !order.getVendor().getArFullName().isEmpty()
                            ? order.getVendor().getArFullName() : order.getVendor().getFullName(),
                    status.equals(WAITING) ? "created" : status.name(), this.getArabicStatus(status));
            notificationService.sendPushNotificationByToken(NotificationPayload.builder()
                    .token(session.getDeviceToken())
                    .title(notificationTitle +" "+ order.getOrderNumber())
                    .body(notificationBody)
                    .build());

            if (status.equals(DELIVERED)
                    && !invoice.getStatus().equals(InvoiceStatus.PAID)) {
                invoiceService.payOrderInvoice(order.getOrderId(), null);
            }
//            iFF
            return true;
        }
        return false;
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
        // long vendorSequenceNumber = sequencesService.getNextSequenceFor("vendors",
        // vendor.getVendorId());
        // long customerSequenceNumber =
        // sequencesService.getNextSequenceFor("customers", customer.getCustomerId());
        // String vendorPrefix = vendor.getFullName().length() < 2 ? ""
        // : vendor.getFullName().substring(0, 2).toUpperCase();
        // String customerPrefix = customer.getEmail() == null ||
        // customer.getEmail().length() < 2 ? ""
        // : customer.getEmail().substring(0, 2).toUpperCase();
        int dayNumberFromToday = now.getDayOfMonth();
        // String orderNumber = String.format("%s%s%04d%04d%04d%02d", customerPrefix,
        // vendorPrefix,
        // vendorSequenceNumber, customerSequenceNumber, orderSequenceNumber,
        // dayNumberFromToday);
        String orderNumber = String.format("%s%04d%02d", "RAT", orderSequenceNumber, dayNumberFromToday);
        return orderNumber;
    }

    private boolean isDeliveredOrCanceledAndThrown(Order order) throws TabaldiGenericException {
        if (order.getStatus().equals(CANCELED)) {
            String orderStatus = CANCELED.name().toLowerCase();
            String orderStatusMessage = MessagesUtils.getOrderStatusMessage(messageSource, orderStatus, orderStatus);
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, orderStatusMessage);
        } else if (order.getStatus().equals(DELIVERED)) {
            String orderStatus = DELIVERED.name().toLowerCase();
            String orderStatusMessage = MessagesUtils.getOrderStatusMessage(messageSource, orderStatus, orderStatus);
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, orderStatusMessage);
        } else {
            return false;
        }
    }

    private void validateDeliveryDistance(Vendor vendor, double orderTotal, int orderDistance)
            throws TabaldiGenericException {
        double minCharge = 25;
        if (vendor.getVendorType() == VendorType.STORE || vendor.getMaxKilometerDelivery() == null) {
            return; // Not applicable for store vendors or those without max delivery
                    // distance
        } else if (vendor.getVendorType().equals(VendorType.RESTAURANT)) {
            minCharge = this.getMinChargeBasedOn(orderDistance);
        }
        // Order total is less than min charge of the distance in case of restaurants,
        // and if it's less than 25 for any distance in case of groceries
        logger.info(
                "Order Total " + orderTotal + " less than Min Charge " + minCharge + " " + (orderTotal < minCharge));
        if (orderTotal < minCharge) {
            String errorMessage = "";
            if (vendor.getVendorType().equals(VendorType.RESTAURANT))
                errorMessage = MessagesUtils.getOrderExceededScopeMessage(messageSource,
                        String.valueOf(minCharge), String.valueOf(minCharge));
            else
                errorMessage = MessagesUtils.getOrderLessThatMinChargeMessage(messageSource,
                        String.valueOf(minCharge), String.valueOf(minCharge));
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
        }
    }

    private double getMinChargeBasedOn(int orderDistance) throws TabaldiGenericException {
        return switch (orderDistance) {
            case 0, 1, 2, 3, 4, 5 -> 20;
            case 6, 7, 8, 9, 10 -> 25;
            case 11, 12, 13, 14, 15 -> 30;
            case 16, 17, 18, 19, 20 -> 40;
            case 21, 22, 23, 24, 25 -> 60;
            case 26, 27, 28, 29, 30 -> 80;
            case 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 -> 100;
            // 40 KM is Maximum Distance for restaurants
            default -> throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, "Distance out of range");
        };
    }

    private String getArabicStatus(OrderStatus status) {
        return switch (status) {
            case DELIVERED -> "توصيل";
            case CANCELED -> "إلغاء";
            case WAITING -> "إنشاء";
            case PROCESSING -> "معالجت";
            case CONFIRMED -> "تأكيد";
        };
    }
}
