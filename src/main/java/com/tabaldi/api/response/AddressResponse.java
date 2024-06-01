package com.tabaldi.api.response;

import com.tabaldi.api.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse extends GenericResponse{
    private String event;
    private Address address;
}
