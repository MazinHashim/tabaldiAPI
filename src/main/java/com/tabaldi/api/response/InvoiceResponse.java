package com.tabaldi.api.response;

import com.tabaldi.api.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponse extends GenericResponse{
    private String event;
    private Invoice invoice;
}
