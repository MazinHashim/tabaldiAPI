package com.tabaldi.api.response;

import com.tabaldi.api.model.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderMapper {

    private long orderId;
    private String orderNumber;
    private String orderDate;
    private double total;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private List<CartItem> cartItems;
    private Customer customer;
    private Address address;
    private Vendor vendor;

    @Builder(builderMethodName = "mappedBuilder")
    public OrderMapper(Order order) {
        this.setOrderId(order.getOrderId());
        this.setOrderNumber(order.getOrderNumber());
        this.setStatus(order.getStatus());
        this.setTotal(order.getTotal());
        this.setOrderDate(order.getOrderDate().format(DateTimeFormatter.ofPattern("YYYY-MM-dd (HH:ss a)")));
        this.setCartItems(order.getCartItems());
        this.setCustomer(order.getCustomer());
        this.setAddress(order.getAddress());
        this.setVendor(order.getVendor());
    }
}
