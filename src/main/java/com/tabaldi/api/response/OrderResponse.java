package com.tabaldi.api.response;

import com.tabaldi.api.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse extends GenericResponse {
    private String event;
    private Order order;
}
