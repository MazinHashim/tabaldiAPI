package com.tabaldi.api.response;

import com.tabaldi.api.model.UserEntity;
import com.tabaldi.api.model.Vendor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class VendorResponse extends GenericResponse {
    private String event;
    private Vendor vendor;

}
