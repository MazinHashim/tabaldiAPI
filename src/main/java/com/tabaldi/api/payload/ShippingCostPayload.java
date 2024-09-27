package com.tabaldi.api.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingCostPayload {

    private double distance;
    private double shippingCost;
    private long vendorId;
}
