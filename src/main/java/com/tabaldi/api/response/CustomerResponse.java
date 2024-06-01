package com.tabaldi.api.response;

import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse extends GenericResponse {
    private String event;
    private Customer customer;
}
