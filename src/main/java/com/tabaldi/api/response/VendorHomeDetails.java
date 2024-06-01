package com.tabaldi.api.response;

import com.tabaldi.api.payload.PendingOrders;
import com.tabaldi.api.payload.ProductFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorHomeDetails {
    private double earnings;
    private long numberOfProducts;
    private long numberOfOrders;
    private PendingOrders pendingOrders;
    private List<ProductFrequency> frequentProducts;
}
