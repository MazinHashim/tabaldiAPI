package com.tabaldi.api.response;

import com.tabaldi.api.model.Order;
import com.tabaldi.api.payload.CustomerFrequency;
import com.tabaldi.api.payload.VendorFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminHomeDetails{
    private double earnings;
    private long numberOfOrders;
    private long newOrdersPerDay;
    private long numberOfCustomers;
    private long newCustomersPer2Days;
    private List<Order> orders;
    private List<CustomerFrequency> frequentCustomers;
    private List<VendorFrequency> frequentVendors;
}
