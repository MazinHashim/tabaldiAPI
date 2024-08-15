package com.tabaldi.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String subtitle;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isShown;
    @Column(nullable = false)
    private String adsImage1;
    @Column(nullable = false)
    private String adsImage2;
    @Column(nullable = false)
    private String adsImage3;
    private String url;
    @Column(nullable = false)
    @JsonIgnore
    private OffsetDateTime expireIn;
    @Column(nullable = false)
    @JsonIgnore
    private OffsetDateTime createdAt;
    @JoinColumn(name = "vendor_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    private Vendor vendor;

    public String getFExpireIn(){
        return expireIn.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a"));
    }
    public String getFCreatedAt(){
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a"));
    }
}
