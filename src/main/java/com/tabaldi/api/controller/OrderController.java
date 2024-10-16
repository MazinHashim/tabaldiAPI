package com.tabaldi.api.controller;

import com.ibm.icu.text.ArabicShapingException;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.model.OrderStatus;
import com.tabaldi.api.payload.*;
import com.tabaldi.api.response.*;
import com.tabaldi.api.service.NotificationService;
import com.tabaldi.api.service.OrderService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class
OrderController {

    private final OrderService orderService;
    private final MessageSource messageSource;
    private final NotificationService notificationService;
    @GetMapping("/{orderId}")
    public @ResponseBody ResponseEntity<OrderResponse> getById (@PathVariable("orderId") Long orderId)
            throws TabaldiGenericException {
        Order order = orderService.getOrderById(orderId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Order", "الطلب");

        return ResponseEntity.ok(OrderResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .order(order).build());

    }
    @GetMapping("/history")
    public @ResponseBody ResponseEntity<ListResponse<Order>> getOrdersHistoryList
            (@RequestParam(required = false) Long customerId) throws TabaldiGenericException, IOException {
        List<Order> ordersList = orderService.getAllOrders(customerId).stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .toList();
        orderService.fillOrdersDetails(ordersList);
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Customers Orders", "طلبات الزبائن");

        return ResponseEntity.ok(
                ListResponse.<Order>genericBuilder()
                        .list(ordersList)
                        .message(fetchMessage)
                        .build()
        );
    }
    @GetMapping("/pending")
    public @ResponseBody ResponseEntity<ListResponse<Order>> getPendingOrdersList() throws TabaldiGenericException, IOException {
        PendingOrders ordersList = orderService.getPendingOrdersList(null);
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Customers Orders", "طلبات الزبائن");

        return ResponseEntity.ok(
                ListResponse.<Order>genericBuilder()
                    .list(ordersList.getOrders())
                    .message(fetchMessage)
                    .build()
        );
    }
    @GetMapping("/pending/{customerId}")
    public @ResponseBody ResponseEntity<ListResponse<Order>> getCustomerPendingOrdersList
            (@PathVariable("customerId") Long customerId) throws TabaldiGenericException, IOException {
        PendingOrders ordersList = orderService.getPendingOrdersList(customerId);
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Customers Orders", "طلبات الزبائن");

        return ResponseEntity.ok(
                ListResponse.<Order>genericBuilder()
                        .list(ordersList.getOrders())
                        .message(fetchMessage)
                        .build()
        );
    }

    @PostMapping("/create/{customerId}")
    public @ResponseBody ResponseEntity<ListResponse<Order>> createOrder (
            @PathVariable("customerId") @Valid long customerId,
            @RequestBody @Valid OrderPayload payload) throws TabaldiGenericException, IOException, ArabicShapingException {

        List<Order> orders = orderService.createAndSaveOrderInfo(customerId, payload);
        String event = "created";
        String successSaveMessage = MessagesUtils.getSavedDataMessage(messageSource,
                "Order", "طلباتك", event, "إنشاء");
        return ResponseEntity.ok(
                ListResponse.<Order>genericBuilder()
                        .list(orders)
                        .message(successSaveMessage)
                        .build());
    }

    @GetMapping("/change/status/{orderId}")
    public @ResponseBody ResponseEntity<ChangedStatusResponse> changeOrderStatus (@PathVariable("orderId") Long orderId,
                                                                       @RequestParam("status") String status)
            throws TabaldiGenericException, IOException {
        OrderStatus orderStatus = this.getOrderStatusFromString(status);
        Boolean isChanged = orderService.changeOrderStatusById(orderId, orderStatus);
        String successChangeMessage = MessagesUtils.getStatusChangedMessage(messageSource, status.toLowerCase(), status.toLowerCase());

        return ResponseEntity.ok(ChangedStatusResponse.builder()
                .message(successChangeMessage)
                .isChanged(isChanged).build());

    }
    @PostMapping("/will/pass/{customerId}")
    public @ResponseBody ResponseEntity<OrderWillPassResponse> checkIfOrderWillPass (@PathVariable("customerId") Long customerId,
                                                                                     @RequestBody List<ShippingCostPayload> shippingCostPayloads)
            throws TabaldiGenericException, IOException {
        Boolean isWillPass = orderService.checkIfOrderWillPass(customerId, shippingCostPayloads);
        String successOrderWillPassMessage = messageSource.getMessage("success.order.will.pass",
                null, LocaleContextHolder.getLocale());

        return ResponseEntity.ok(OrderWillPassResponse.builder()
                .message(successOrderWillPassMessage)
                .isWillPass(isWillPass).build());

    }
    @PostMapping("/save/note/{orderId}")
    public @ResponseBody ResponseEntity<OrderVendorNoteResponse> saveVendorNote (@PathVariable("orderId") Long orderId,
                                                                                  @RequestBody NotePayload payload)
            throws TabaldiGenericException {
        String vendorNote = orderService.saveVendorNote(orderId, payload.getVendorNote());
        String successChangeMessage = MessagesUtils.getSavedDataMessage(messageSource, "Order", "الطلب", "updated", "تعديل");

        return ResponseEntity.ok(OrderVendorNoteResponse.builder()
                .message(successChangeMessage)
                .vendorNote(vendorNote).build());

    }

    private OrderStatus getOrderStatusFromString(String status) {
            switch (status){
                case "WAITING":
                    return OrderStatus.WAITING;
                case "PROCESSING":
                    return OrderStatus.PROCESSING;
                case "CONFIRMED":
                    return OrderStatus.CONFIRMED;
                case "DELIVERED":
                    return OrderStatus.DELIVERED;
                case "CANCELED":
                    return OrderStatus.CANCELED;
            }
            return null;
    }

    @GetMapping("/{orderId}/cartItems")
    public @ResponseBody ResponseEntity<ListResponse<CartItem>> getOrderCartItemsList (
            @PathVariable("orderId") Long orderId) throws TabaldiGenericException, IOException {
        List<CartItem> cartItemsList = orderService.getOrderCartItemsList(orderId);
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Cart Items", "أغراض السلة");
        return ResponseEntity.ok(
                ListResponse.<CartItem>genericBuilder()
                        .list(cartItemsList)
                        .message(fetchMessage)
                        .build()
        );
    }
}
