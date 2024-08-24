package com.tabaldi.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
//
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", unique = true, nullable = false)
    private long orderId;
    @Column(nullable = false)
    private String orderNumber;
    private String vendorNotes;
    private String comment;
    @Column(nullable = false)
    private OffsetDateTime orderDate;
    private OffsetDateTime processedDate;
    private OffsetDateTime deliveredDate;
    @Transient
    private double total;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @OneToMany(mappedBy = "order")
    private List<CartItem> cartItems;

    @JoinColumn(name = "customer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ManyToOne
    private Customer customer;

    @JoinColumn(name = "address_id", nullable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ManyToOne
    private Address address;

    @JoinColumn(name = "vendor_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    private Vendor vendor;

    public String getFOrderDate(){
        return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a"));
    }
    public String getFProcessedDate(){
        return processedDate==null?null:processedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a"));
    }
    public String getFDeliveredDate(){
        return deliveredDate==null?null:deliveredDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a"));
    }
}
