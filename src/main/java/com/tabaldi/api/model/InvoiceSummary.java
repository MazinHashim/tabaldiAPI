package com.tabaldi.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invoice_summary")
public class InvoiceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id", unique = true, nullable = false)
    private long summaryId;
    @Column(nullable = false)
    private double subtotal;
    @Column(nullable = false)
    private double discount;
    @Column(nullable = false)
    private double taxes;
    @Column(nullable = false)
    private double shippingCost;
    @Column(nullable = false)
    private double total;
}
