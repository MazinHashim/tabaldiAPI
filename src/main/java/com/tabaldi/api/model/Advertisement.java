package com.tabaldi.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.*;
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
    private Long advertisementId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String arTitle;
    private String subtitle;
    private String arSubtitle;
    @Column(nullable = false, columnDefinition = "int default 1")
    private int priority;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isShown;
    @Column(nullable = false)
    private String adsImage1;
    // @Column(nullable = false)
    // private String adsImage2;
    // @Column(nullable = false)
    // private String adsImage3;
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

    public String getFExpireDate() {
        return expireDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getFCreatedDate() {
        return createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getFStartTime() {
        return startTime.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }

    public String getFEndTime() {
        return endTime.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }

//========================= check if it's not expired until specified end time ONLY =========================/
    public boolean isExpiredNow(){
        LocalDateTime timeInUAE = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.ofHours(4));
        LocalDateTime startingDateTime = this.createdDate.atTime(this.startTime);
        LocalDateTime endingDateTime = this.expireDate.atTime(this.endTime);
        return (timeInUAE.isAfter(startingDateTime) && timeInUAE.isBefore(endingDateTime));
    }

//========================= check if it's not expired and if it's between specified time range per day =========================/
//    public boolean isVisibleNow(){
//        LocalDateTime timeInUAE = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.ofHours(4));
//        LocalDateTime startingDateTime = LocalDateTime.now();
//        startingDateTime = startingDateTime.withHour(this.startTime.getHour());
//        startingDateTime = startingDateTime.withMinute(this.startTime.getMinute());
//        LocalDateTime endingDateTime = LocalDateTime.now();
//        endingDateTime = endingDateTime.withHour(this.endTime.getHour());
//        endingDateTime = endingDateTime.withMinute(this.endTime.getMinute());
//        if(this.startTime.isAfter(this.endTime) || this.startTime.equals(this.endTime)){
//            endingDateTime = endingDateTime.plusDays(1);
//        }
//        return (LocalDate.now().isAfter(this.getCreatedDate()) && LocalDate.now().isBefore(this.getExpireDate()))
//                && (timeInUAE.isAfter(startingDateTime) && timeInUAE.isBefore(endingDateTime));
//    }
}
