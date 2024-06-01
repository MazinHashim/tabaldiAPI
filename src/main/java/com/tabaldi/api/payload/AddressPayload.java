package com.tabaldi.api.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressPayload {

    private Long addressId;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private String street;
    @NotNull
    @NotEmpty
    private String phone;
    @NotNull
    private double latitude;
    @NotNull
    private double longitude;
    @NotNull
    private Long customerId;
}
