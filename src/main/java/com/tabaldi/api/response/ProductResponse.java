package com.tabaldi.api.response;

import com.tabaldi.api.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse extends GenericResponse{
    private String event;
    private Product product;
}
