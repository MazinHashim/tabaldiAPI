package com.tabaldi.api.controller;

import com.ibm.icu.text.ArabicShapingException;
import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Invoice;
import com.tabaldi.api.response.InvoiceResponse;
import com.tabaldi.api.response.DeleteResponse;
import com.tabaldi.api.service.InvoiceService;
import com.tabaldi.api.service.PdfGeneratorService;
import com.tabaldi.api.utils.MessagesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfGeneratorService pdfGeneratorService;
    private final MessageSource messageSource;

    @GetMapping("/order/{orderId}")
    public @ResponseBody ResponseEntity<InvoiceResponse> getByOrderId (@PathVariable("orderId") Long orderId)
            throws TabaldiGenericException, IOException {
        Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
        String successFetchMessage = MessagesUtils.getFetchMessage(messageSource, "Invoice", "الفاتورة");

        return ResponseEntity.ok(InvoiceResponse.builder()
                .message(successFetchMessage)
                .event("fetched")
                .invoice(invoice).build());

    }
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) throws TabaldiGenericException, IOException, ArabicShapingException {
        Invoice invoice = invoiceService.getInvoiceById(id);
        byte[] pdfData = pdfGeneratorService.generatePdf(invoice, false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_" + invoice.getInvoiceNumber() + ".pdf");

        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
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
