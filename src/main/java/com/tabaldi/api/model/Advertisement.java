package com.tabaldi.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
    private String subTitle;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isShown;
    @Column(nullable = false)
    private String adsImage;
    private String url;
    @Column(nullable = false)
    private OffsetDateTime expireIn;
    @JoinColumn(name = "vendor_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    private Vendor vendor;

    public String getFExpireIn(){
        return expireIn.format(DateTimeFormatter.ofPattern("DD-MM-yyyy HH:mm a"));
    }
}
