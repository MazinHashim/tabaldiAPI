package com.tabaldi.api.response;

import com.tabaldi.api.payload.PendingOrders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PendingOrderResponse extends GenericResponse {
    private String event;
    private PendingOrders order;
}
