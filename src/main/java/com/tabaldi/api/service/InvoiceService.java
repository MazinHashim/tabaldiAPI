package com.tabaldi.api.service;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Invoice;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.payload.CardPayload;
import com.tabaldi.api.payload.InvoicePayload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface InvoiceService {

    Invoice getInvoiceById(long invoiceId) throws TabaldiGenericException;
    Invoice getInvoiceByOrderId(long orderId) throws TabaldiGenericException, IOException;
    Invoice saveInvoiceInfo(InvoicePayload payload, Order order) throws TabaldiGenericException;
    Boolean deleteInvoiceById(Long invoiceId) throws TabaldiGenericException;
    Invoice payOrderInvoice(Long orderId, CardPayload cardPayload) throws TabaldiGenericException, IOException;
    List<Invoice> getInvoicesList(long vendorId) throws TabaldiGenericException;
}
