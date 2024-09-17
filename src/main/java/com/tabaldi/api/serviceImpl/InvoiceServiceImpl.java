package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.*;
import com.tabaldi.api.payload.*;
import com.tabaldi.api.repository.InvoiceRepository;
import com.tabaldi.api.repository.InvoiceSummaryRepository;
import com.tabaldi.api.service.InvoiceService;
import com.tabaldi.api.service.PaymentService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentService paymentService;
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
            Random random = new Random();
            int invoiceNumber = random.ints(111111, 999999).findFirst().getAsInt();

            return invoiceRepository.save(Invoice.builder()
                    .invoiceNumber(String.valueOf(invoiceNumber))
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
    public Invoice payOrderInvoice(Long orderId, CardPayload cardPayload) throws TabaldiGenericException, IOException {
        Invoice invoice = this.getInvoiceByOrderId(orderId);
        if(invoice.getStatus().equals(InvoiceStatus.PAID)){
            String notDeliveredMessage = messageSource.getMessage("error.invoice.already.paid", null, LocaleContextHolder.getLocale());
            throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notDeliveredMessage);
        } else if(invoice.getPaymentMethod().equals(PaymentMethod.CASH)) {
            if (!invoice.getOrder().getStatus().equals(OrderStatus.DELIVERED)) {
                String notDeliveredMessage = messageSource.getMessage("error.not.delivered", null, LocaleContextHolder.getLocale());
                throw new TabaldiGenericException(HttpServletResponse.SC_BAD_REQUEST, notDeliveredMessage);
            } else {
                invoice.setStatus(InvoiceStatus.PAID);
                return invoiceRepository.save(invoice);
            }
        } else if (invoice.getOrder().getStatus().equals(OrderStatus.WAITING)) {
            // Initialize online invoice payment
            InitPaymentPayload initPaymentPayload = InitPaymentPayload.builder()
                    .InvoiceAmount(invoice.getSummary().getTotal())
                    .CurrencyIso("AED")
                    .build();
            Map<String, Object> paymentMethodsResponse = paymentService.initializeMyFatoorahPayment(initPaymentPayload);
            System.out.println("Payment Methods: "+paymentMethodsResponse.toString());

            // Execute online invoice payment
            ExecutePaymentPayload executePaymentPayload = ExecutePaymentPayload.builder()
                    .InvoiceValue(invoice.getSummary().getTotal())
                    .PaymentMethodId(20)
                    .build();
            Map<String, Object> executePaymentResponse = paymentService.executePaymentTransaction(executePaymentPayload);
            System.out.println("Execute Payment: "+executePaymentResponse.toString());
            if(invoice.getPaymentMethod().equals(PaymentMethod.VISA)
                    || invoice.getPaymentMethod().equals(PaymentMethod.MASTER_CARD)) {
                // Directly pay online invoice payment
                DirectPaymentPayload directPaymentPayload = DirectPaymentPayload.builder()
                        .Bypass3DS(true)
                        .PaymentType("Card")
                        .SaveToken(false)
                        .Card(cardPayload)
                        .build();
                Map<String, Object> data = (HashMap) executePaymentResponse.get("Data");
                Map<String, Object> directPaymentResponse = paymentService.directPaymentTransaction(directPaymentPayload, data.get("PaymentURL").toString());
                System.out.println("Direct Payment: " + directPaymentResponse.toString());
            } else if(invoice.getPaymentMethod().equals(PaymentMethod.APPLE_PAY)){
                // add apple_pay integration
            }
            invoice.setStatus(InvoiceStatus.PAID);
//            invoice.setInvoiceNumber("4303866");
            return invoiceRepository.save(invoice);
        } else
            return invoice;
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
