package com.tabaldi.api.payload;

import com.tabaldi.api.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingOrders {
    private List<Order> orders;
    private long count;
}
