package com.tabaldi.api.service;

import com.ibm.icu.text.ArabicShapingException;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.model.OrderStatus;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.payload.OrderPayload;
import com.tabaldi.api.payload.PendingOrders;
import com.tabaldi.api.payload.ShippingCostPayload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface OrderService {
    List<Order> getAllOrders(Long customerId) throws TabaldiGenericException;
    List<Order> getByVendor(Vendor vendor, boolean check) throws TabaldiGenericException;
    List<Order> createAndSaveOrderInfo(long customerId, OrderPayload payload) throws HttpClientErrorException, TabaldiGenericException, IOException, ArabicShapingException;
    Map<String, Long> countAllOrdersInSystem() throws TabaldiGenericException;
    PendingOrders fetchPendingOrdersByVendor(List<Order> orders);
    PendingOrders getPendingOrdersList(Long customerId) throws TabaldiGenericException, IOException;
    double fetchCompanyEarningsFromOrders(List<Order> orders);
    double fetchVendorEarningsFromOrders(List<Order> orders);
    Boolean changeOrderStatusById(Long orderId, OrderStatus status) throws TabaldiGenericException, IOException;

    Order getOrderById(Long orderId) throws TabaldiGenericException;

    List<CartItem> getOrderCartItemsList(Long orderId) throws TabaldiGenericException, IOException;

    void fillOrdersDetails(List<Order> orderList) throws IOException, TabaldiGenericException;
    void fillOrderDetails(Order order) throws TabaldiGenericException, IOException;

    String saveVendorNote(Long orderId, String vendorNote) throws TabaldiGenericException;

    Boolean checkIfOrderWillPass(Long customerId, List<ShippingCostPayload> payload) throws TabaldiGenericException, IOException;
}
