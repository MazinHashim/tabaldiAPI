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
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", unique = true, nullable = false)
    private long productId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int quantity;
    @Column(length = 2000, nullable = false)
    @JsonIgnore
    private String imagesCollection;
    @Transient
    private List<String> images;
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private double companyProfit;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isPublished;

    private String description;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<Option> options;
//    @OneToMany(mappedBy = "product")
//    private List<CartItem> cartItems;
    @JoinColumn(name = "vendor_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
//    @JsonIgnore
    private Vendor vendor;
    @JoinColumn(name = "category_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    private Category category;
}
