package com.tabaldi.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id", unique = true, nullable = false)
    private Integer sessionId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(
            action = OnDeleteAction.CASCADE
    )
    private UserEntity user;
    @Column(nullable = false)
    private String refreshToken;
    private String sessionToken;
    private String deviceToken;
    private LocalDateTime lastLogin;
    private LocalDateTime lastLogout;

}