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
public class OptionPayload {

    private Long optionId;
    @NotEmpty
    private String name;
    private String groupFlag;
    private Double fee;
    @NotNull
    private Long productId;
}
