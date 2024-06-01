package com.tabaldi.api.repository;

import com.tabaldi.api.model.Session;
import com.tabaldi.api.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface SessionRepository extends JpaRepository<Session, Integer> {


    Optional<Session> findByUser(UserEntity user);

    Optional<Session> findByRefreshToken(String refreshToken);
}
