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
public class CategoryPayload {

    private Long categoryId;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private String arName;
    boolean isPublished;
    @NotNull
    private Long vendorId;
}
