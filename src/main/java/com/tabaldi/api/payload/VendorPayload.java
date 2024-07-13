package com.tabaldi.api.payload;

import com.tabaldi.api.model.VendorType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorPayload {

    private Long vendorId;
    @NotNull
    @NotEmpty
    private String fullName;
    @NotNull
    private VendorType vendorType;
    private Integer maxKilometerDelivery;
    @NotNull
    private LocalTime openingTime;
    @NotNull
    private LocalTime closingTime;
    private Integer minChargeLongDistance;
    @NotNull
    private Long userId;
}
