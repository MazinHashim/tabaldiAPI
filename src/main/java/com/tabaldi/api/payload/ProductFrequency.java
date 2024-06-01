package com.tabaldi.api.payload;

import com.tabaldi.api.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductFrequency {
    private Product product;
    private long frequency;
}
