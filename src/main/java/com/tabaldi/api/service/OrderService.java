package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.model.OrderStatus;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.payload.PendingOrders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface OrderService {
    List<Order> getAllOrders();
    List<Order> getByVendor(Vendor vendor) throws TabaldiGenericException;
    List<Order> createAndSaveOrderInfo(long customerId) throws TabaldiGenericException, IOException;
    Map<String, Long> countAllOrdersInSystem() throws TabaldiGenericException;
    PendingOrders fetchActiveOrdersByVendor(List<Order> orders);
    double fetchCompanyEarningsFromOrders(List<Order> orders);
    double fetchVendorEarningsFromOrders(List<Order> orders);
    Boolean changeOrderStatusById(Long orderId, OrderStatus status) throws TabaldiGenericException;

    Order getOrderById(Long orderId) throws TabaldiGenericException;

    List<CartItem> getOrderCartItemsList(Long orderId) throws TabaldiGenericException, IOException;

    void fillOrdersDetails(List<Order> orderList);

}
