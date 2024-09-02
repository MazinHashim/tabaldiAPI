package com.tabaldi.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "advertisements")
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "advertisement_id", unique = true, nullable = false)
    private long advertisementId;
    @Column(nullable = false)
    private String title;
    private String subtitle;
    @Column(nullable = false, columnDefinition = "int default 1")
    private int priority;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isShown;
    @Column(nullable = false)
    private String adsImage1;
//    @Column(nullable = false)
//    private String adsImage2;
//    @Column(nullable = false)
//    private String adsImage3;
    private String url;
    @Column(nullable = false)
    private LocalDate createdDate;
    @Column(nullable = false)
    private LocalTime startTime;
    @Column(nullable = false)
    private LocalDate expireDate;
    @Column(nullable = false)
    private LocalTime endTime;
    @JoinColumn(name = "vendor_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    private Vendor vendor;

    public String getFExpireDate(){
        return expireDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public String getFCreatedDate(){
        return createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public String getFStartTime(){
        return startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss a"));
    }
    public String getFEndTime(){
        return endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss a"));
    }
}
