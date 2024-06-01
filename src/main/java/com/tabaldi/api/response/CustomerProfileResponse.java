package com.tabaldi.api.response;

import com.tabaldi.api.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProfileResponse extends CustomerResponse {
    private boolean newUser;
}
