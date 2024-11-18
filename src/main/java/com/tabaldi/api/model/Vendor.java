package com.tabaldi.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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
    @Column(nullable = false)
    private String arFullName;
    private String profileImage;
    private String coverImage;
    @Column(nullable = false)
    private String identityImage;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TabaldiRegion region;
    @Column(nullable = false)
    private String licenseImage;
    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lng;
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
    @Transient
    private int inactiveProductsCount;
    @Transient
    private int inactiveCategoriesCount;
    @Transient
    private String userPhone;
    @Transient
    private String userEmail;
    @Transient
    private Long userId;
//    @OneToMany(mappedBy = "vendor")
//    private List<Category> categories;
//    @OneToMany(mappedBy = "vendor")
//    private List<Product> products;
//    @OneToMany(mappedBy = "vendor")
//    private List<Orders> orders;
//    @OneToMany(mappedBy = "vendor")
//    private List<UserEntity> users;

    public String getFOpeningTime(){
        return openingTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
    public String getFClosingTime(){
        return closingTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
    public boolean isStillOpening(){
        LocalDateTime timeInUAE = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.ofHours(4));
        LocalDateTime openingDateTime = LocalDateTime.now().withSecond(0).withNano(0);
        openingDateTime = openingDateTime
                .withHour(this.openingTime.getHour())
                .withMinute(this.openingTime.getMinute());
        LocalDateTime closingDateTime = LocalDateTime.now().withSecond(0).withNano(0);
        closingDateTime = closingDateTime
                .withHour(this.closingTime.getHour())
                .withMinute(this.closingTime.getMinute());
        if(this.openingTime.isAfter(this.closingTime) || this.openingTime.equals(this.closingTime)){
            closingDateTime = closingDateTime.plusDays(1);
        }
        return timeInUAE.isAfter(openingDateTime) && timeInUAE.isBefore(closingDateTime);
    }

    public boolean isEquals() {return this.openingTime.equals(this.closingTime);}

}
