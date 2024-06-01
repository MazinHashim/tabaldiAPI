package com.tabaldi.api.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_verifications")
public class UserVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_verification_id", unique = true, nullable = false)
    private long userVerificationId;
    @Column(nullable = false)
    private int code;
    @Column(nullable = false)
    private OffsetDateTime createdTime;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private OffsetDateTime expiryTime;
    private OffsetDateTime verifiedTime;
    @Column(nullable = false)
    private VerificationStatus status;
    @Column(nullable = false)
    private String keyRef;
    @Column(nullable = false)
    private int resendCounter;

    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    private UserEntity user;
}
