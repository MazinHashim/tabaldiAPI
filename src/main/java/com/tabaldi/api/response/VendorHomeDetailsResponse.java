package com.tabaldi.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class VendorHomeDetailsResponse extends GenericResponse{
    private String event;
    private VendorHomeDetails details;
}
