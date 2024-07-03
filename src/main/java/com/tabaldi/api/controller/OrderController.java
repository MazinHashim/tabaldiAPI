package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.model.OrderStatus;
import com.tabaldi.api.payload.PendingOrders;
import com.tabaldi.api.response.*;
import com.tabaldi.api.service.OrderService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final MessageSource messageSource;

    @GetMapping("/{orderId}")
    public @ResponseBody ResponseEntity<OrderResponse> getById (@PathVariable("orderId") Long orderId)
            throws TabaldiGenericException {
        Order order = orderService.getOrderById(orderId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Order", "الطلب");

        return ResponseEntity.ok(OrderResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .order(OrderMapper.mappedBuilder().order(order).build()).build());

    }
    @GetMapping("/pending")
    public @ResponseBody ResponseEntity<ListResponse<OrderMapper>> getPendingOrdersList () throws TabaldiGenericException {
        PendingOrders ordersList = orderService.getPendingOrdersList(); // may add filters
        String fetchMessage = MessagesUtils.getFetchMessage(messageSource, "Customers Orders", "طلبات الزبائن");

        return ResponseEntity.ok(
                ListResponse.<OrderMapper>genericBuilder()
                    .list(ordersList.getOrders())
                    .message(fetchMessage)
                    .build()
        );
    }

    @PostMapping("/create/{customerId}")
    public @ResponseBody ResponseEntity<ListResponse<Order>> createOrder (
            @PathVariable("customerId") @Valid long customerId) throws TabaldiGenericException, IOException {

        List<Order> orders = orderService.createAndSaveOrderInfo(customerId);
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
            throws TabaldiGenericException {
        OrderStatus orderStatus = this.getOrderStatusFromString(status);
        Boolean isChanged = orderService.changeOrderStatusById(orderId, orderStatus);
        String successChangeMessage = MessagesUtils.getStatusChangedMessage(messageSource, status.toLowerCase(), status.toLowerCase());

        return ResponseEntity.ok(ChangedStatusResponse.builder()
                .message(successChangeMessage)
                .isChanged(isChanged).build());

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
