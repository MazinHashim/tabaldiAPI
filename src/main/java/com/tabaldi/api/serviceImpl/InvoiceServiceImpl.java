package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.InvoicePayload;
import com.tabaldi.api.repository.InvoiceRepository;
import com.tabaldi.api.repository.InvoiceSummaryRepository;
import com.tabaldi.api.service.InvoiceService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceSummaryRepository summaryRepository;
    private final MessageSource messageSource;

    @Override
    public Invoice getInvoiceById(long invoiceId) throws TabaldiGenericException {
        Optional<Invoice> invoiceOptional = invoiceRepository.findById(invoiceId);
        if (!invoiceOptional.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Invoice", "الفاتورة");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return invoiceOptional.get();
    }

    @Override
    public Invoice getInvoiceByOrderId(long orderId) throws TabaldiGenericException {
        Optional<Invoice> invoiceOptional = invoiceRepository.findByOrderId(orderId);
        if (!invoiceOptional.isPresent()) {
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource, "Invoice", "الفاتورة");
            throw new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        }
        return invoiceOptional.get();
    }

    @Override
    public Invoice saveInvoiceInfo(InvoicePayload payload, Order order) throws TabaldiGenericException {
        if(!order.getStatus().equals(OrderStatus.WAITING)){
            String notConfirmedMessage = messageSource.getMessage("error.not.waiting", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notConfirmedMessage);
        } else {

            InvoiceSummary summary = summaryRepository.save(InvoiceSummary.builder()
                    .discount(payload.getDiscount())
                    .taxes(payload.getTaxes())
                    .shippingCost(payload.getShippingCost())
                    .subtotal(payload.getSubtotal())
                    .total(payload.getTotal())
                    .build());

            return invoiceRepository.save(Invoice.builder()
                    .invoiceNumber("INV-" + UUID.randomUUID())
                    .issueDate(OffsetDateTime.now())
                    .paymentMethod(payload.getPaymentMethod())
                    .status(InvoiceStatus.UNPAID)
                    .order(order)
                    .summary(summary)
                    .build());
        }
    }

    @Override
    public Boolean deleteInvoiceById(Long invoiceId) throws TabaldiGenericException {
        Optional<Invoice> invoiceOptional = invoiceRepository.findById(invoiceId);
        if (!invoiceOptional.isPresent()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"Invoice", "الفاتورة");
            throw  new TabaldiGenericException(HttpServletResponse.SC_NOT_FOUND, notFoundMessage);
        } else {
            Invoice invoice = invoiceOptional.get();
            invoiceRepository.deleteById(invoice.getInvoiceId());
            return true;
        }
    }

    @Override
    public Invoice changeOrderInvoiceToPaid(Long orderId) throws TabaldiGenericException {
        Invoice invoice = this.getInvoiceByOrderId(orderId);
        if(invoice.getStatus().equals(InvoiceStatus.PAID)){
            // change error to already paid
            String notDeliveredMessage = messageSource.getMessage("error.not.delivered", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notDeliveredMessage);
        } else if(invoice.getPaymentMethod().equals("Cash On Delivery")) {
            if (!invoice.getOrder().getStatus().equals(OrderStatus.CONFIRMED) &&
                    !invoice.getOrder().getStatus().equals(OrderStatus.DELIVERED)) {
                String notDeliveredMessage = messageSource.getMessage("error.not.delivered", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notDeliveredMessage);
            } else if(invoice.getOrder().getStatus().equals(OrderStatus.DELIVERED)){
                invoice.setStatus(InvoiceStatus.PAID);
                return invoiceRepository.save(invoice);
            } else {
                return invoice;
            }
        } else if (!invoice.getOrder().getStatus().equals(OrderStatus.CONFIRMED)) {
            // change error to not confirmed
            String notDeliveredMessage = messageSource.getMessage("error.not.delivered", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notDeliveredMessage);
        } else {
            invoice.setStatus(InvoiceStatus.PAID);
            return invoiceRepository.save(invoice);
        }
    }

    @Override
    public List<Invoice> getInvoicesList(long vendorId) throws TabaldiGenericException {
        List<Invoice> invoiceList = invoiceRepository.findByVendorId(vendorId);
        if(invoiceList.isEmpty()){
            String notFoundMessage = MessagesUtils.getNotFoundMessage(messageSource,"invoices", "الفواتير");
            throw new TabaldiGenericException(HttpServletResponse.SC_OK, notFoundMessage);
        }
        return invoiceList.stream().sorted(Comparator.comparing(Invoice::getIssueDate).reversed()).collect(Collectors.toList());
    }
}
