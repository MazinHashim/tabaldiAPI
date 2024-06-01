package com.tabaldi.api.response;

import com.tabaldi.api.model.Vendor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class VendorProfileResponse extends VendorResponse {
    private boolean newUser;

}
