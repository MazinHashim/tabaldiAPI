package com.tabaldi.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", unique = true, nullable = false)
    private long itemId;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private double price;
    @JsonIgnore
    private String optionsCollection;
    @Transient
    private List<Option> selectedOptions;
    private String comment;
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ManyToOne
    private Product product;
    @JoinColumn(name = "customer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    private Customer customer;
    @JoinColumn(name = "order_id")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ManyToOne
    @JsonIgnore
    private Order order;
}
