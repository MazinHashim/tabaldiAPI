package com.tabaldi.api.controller;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Invoice;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.payload.InvoicePayload;
import com.tabaldi.api.response.InvoiceResponse;
import com.tabaldi.api.response.DeleteResponse;
import com.tabaldi.api.response.ListResponse;
import com.tabaldi.api.service.InvoiceService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final MessageSource messageSource;

    @GetMapping("/order/{orderId}")
    public @ResponseBody ResponseEntity<InvoiceResponse> getByOrderId (@PathVariable("orderId") Long orderId)
            throws TabaldiGenericException {
        Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Invoice", "الفاتورة");

        return ResponseEntity.ok(InvoiceResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .invoice(invoice).build());

    }

    @DeleteMapping("/delete/{invoiceId}")
    public @ResponseBody ResponseEntity<DeleteResponse> deleteInvoice (@PathVariable("invoiceId") Long invoiceId)
            throws TabaldiGenericException {
        Boolean isDeleted = invoiceService.deleteInvoiceById(invoiceId);
        String successDeleteMessage = MessagesUtils.getDeletedMessage(messageSource, "Invoice", "الفاتورة");

        return ResponseEntity.ok(DeleteResponse.builder()
                .message(successDeleteMessage)
                .isDeleted(isDeleted).build());

    }
}
