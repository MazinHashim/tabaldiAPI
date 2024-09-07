package com.tabaldi.api.payload;

import com.tabaldi.api.model.Option;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPayload {

    private Long productId;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private String arName;
    @NotNull
    @NotEmpty
    private String duration;
    @NotNull
    private double price;
    @NotNull
    private int quantity;
    @NotNull
    private double companyProfit;
    private String description;
    private String arDescription;
    private List<Option> options;
    @NotNull
    private Long categoryId;
    @NotNull
    private Long vendorId;
}
