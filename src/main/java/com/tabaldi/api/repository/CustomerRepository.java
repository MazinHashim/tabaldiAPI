package com.tabaldi.api.repository;

import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    long countByCustomerDate(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    Optional<Customer> findByUser(UserEntity user);
}
