package com.tabaldi.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id", unique = true, nullable = false)
    private long invoiceId;
    @Column(nullable = false)
    private String invoiceNumber;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
    @Column(nullable = false)
    private OffsetDateTime issueDate;
    @JoinColumn(name = "summary_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    private InvoiceSummary summary;
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    private Order order;

    public String getFIssueDate(){
        return issueDate.format(DateTimeFormatter.ofPattern("dd/mm/YYYY HH:mm a"));
    }
}
