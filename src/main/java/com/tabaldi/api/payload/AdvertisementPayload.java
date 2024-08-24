package com.tabaldi.api.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementPayload {

    private Long advertisementId;
    @NotNull
    @NotEmpty
    private String title;
    private String subtitle;
    private String url;
    @NotNull
    private LocalDate createDate;
    @NotNull
    private LocalDate expireDate;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    @NotNull
    private Long vendorId;
}
