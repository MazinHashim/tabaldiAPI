package com.tabaldi.api.model;

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
@Table(name = "vendors")
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id", unique = true, nullable = false)
    private long vendorId;
    @Column(nullable = false)
    private String fullName;
    private String profileImage;
    @Column(nullable = false)
    private String identityImage;
    @Column(nullable = false)
    private String licenseImage;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VendorType vendorType;
    private Integer maxKilometerDelivery;
    private Integer minChargeLongDistance;
//    @OneToMany(mappedBy = "vendor")
//    private List<Category> categories;
//    @OneToMany(mappedBy = "vendor")
//    private List<Product> products;
//    @OneToMany(mappedBy = "vendor")
//    private List<Orders> orders;
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    private UserEntity user;
}
