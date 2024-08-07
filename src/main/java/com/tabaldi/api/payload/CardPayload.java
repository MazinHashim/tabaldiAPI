package com.tabaldi.api.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CardPayload {
    @NotNull
    @NotEmpty
    @JsonProperty("Number")
    private String Number;
    @NotNull
    @NotEmpty
    @JsonProperty("ExpiryMonth")
    private String ExpiryMonth;
    @NotNull
    @NotEmpty
    @JsonProperty("ExpiryYear")
    private String ExpiryYear;
    @NotNull
    @NotEmpty
    @JsonProperty("SecurityCode")
    private String SecurityCode;
    @JsonProperty("HolderName")
    private String HolderName;
}