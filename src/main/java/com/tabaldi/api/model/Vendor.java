package com.tabaldi.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private String coverImage;
    @Column(nullable = false)
    private String identityImage;
    @Column(nullable = false)
    private String licenseImage;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VendorType vendorType;
    private Integer maxKilometerDelivery;
    private Integer minChargeLongDistance;
    @Column(nullable = false)
    private LocalTime openingTime;
    @Column(nullable = false)
    private LocalTime closingTime;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isWorking;
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

    public String getFOpeningTime(){
        return openingTime.format(DateTimeFormatter.ofPattern("HH:mm a"));
    }
    public String getFClosingTime(){
        return closingTime.format(DateTimeFormatter.ofPattern("HH:mm a"));
    }
}
