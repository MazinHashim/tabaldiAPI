package com.tabaldi.api.payload;

import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.Vendor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorFrequency {
    private Vendor vendor;
    private long frequency;
}
