package com.tabaldi.api.response;

import com.tabaldi.api.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class VendorCategoryResponse {
    private int numberOfProducts;
    private Category category;
}
