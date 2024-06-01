package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.*;
import com.tabaldi.api.response.*;
import com.tabaldi.api.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetailsServiceImpl implements DetailsService {

    private final VendorService vendorService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final MessageSource messageSource;

    @Override
    public AdminHomeDetails getAdminHomeDetails() throws TabaldiGenericException {
        List<Order> orders = orderService.getAllOrders();
        orderService.fillOrdersDetails(orders);
        List<OrderMapper> RecentOrders = getRecentOrders(orders, 5);

        double companyEarnings = orderService.fetchCompanyEarningsFromOrders(orders);
        Map<String, Long> numberOfOrders = orderService.countAllOrdersInSystem();
        Map<String, Long> numberOfCustomers = customerService.countAllCustomerInSystem();
        List<CustomerFrequency> frequentCustomers = customerService.fetchFrequentCustomerByOrders(orders, 5);
        List<VendorFrequency> frequentVendors = vendorService.fetchFrequentVendorByOrders(orders, 5);
        return AdminHomeDetails.builder()
                .earnings(companyEarnings)
                .numberOfCustomers(numberOfCustomers.get("all"))
                .numberOfOrders(numberOfOrders.get("all"))
                .newCustomersPer2Days(numberOfCustomers.get("twoDays"))
                .newOrdersPerDay(numberOfOrders.get("oneDay"))
                .frequentCustomers(frequentCustomers)
                .orders(RecentOrders)
                .frequentVendors(frequentVendors)
                .build();
    }
    @Override
    public VendorHomeDetails getVendorHomeDetails(Long vendorId) throws TabaldiGenericException {
        Vendor vendor = vendorService.getVendorById(vendorId);
        List<Order> orders = orderService.getByVendor(vendor);
        orderService.fillOrdersDetails(orders);

        double vendorEarnings = orderService.fetchVendorEarningsFromOrders(orders);
        Long numberOfProducts = vendorService.countAllProductsPerVendor(vendorId);
        List<ProductFrequency> frequentProducts = this.fetchFrequentProductsByOrders(orders, 5);
        PendingOrders pendingOrders = orderService.fetchActiveOrdersByVendor(orders);
        return VendorHomeDetails.builder()
                .earnings(vendorEarnings)
                .numberOfProducts(numberOfProducts)
                .numberOfOrders(orders.size())
                .frequentProducts(frequentProducts)
                .pendingOrders(pendingOrders)
                .build();
    }

    private List<ProductFrequency> fetchFrequentProductsByOrders(List<Order> orders, int size) {
        return orders.stream()
                .flatMap(order -> order.getCartItems().stream()
                        .map(CartItem::getProduct)
                        .distinct())
                .collect(Collectors.groupingBy(product -> product, Collectors.counting()))  // Step 1: Count frequency
                .entrySet().stream()
                .map(entry -> new ProductFrequency(entry.getKey(), entry.getValue()))  // Step 2: Map to VendorFrequency
                .sorted((vf1, vf2) -> Long.compare(vf2.getFrequency(), vf1.getFrequency()))  // Step 3: Sort by frequency
                .limit(size)
                .collect(Collectors.toList());
    }

    private List<OrderMapper> getRecentOrders(List<Order> orders, int size) {
        return orders.stream()
                .filter(order -> !order.getStatus().equals(OrderStatus.WAITING))
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(size)
                .map(order -> {
                    order.getCartItems().forEach(cartItem -> {
                        cartItem.getProduct().setOptions(null);
                        cartItem.setCustomer(null);
                    });
                    return OrderMapper.mappedBuilder().order(order).build();
                })
                .collect(Collectors.toList());
    }
}
