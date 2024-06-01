package com.tabaldi.api.response;

import com.tabaldi.api.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse extends GenericResponse {
    private String event;
    private OrderMapper order;
}
