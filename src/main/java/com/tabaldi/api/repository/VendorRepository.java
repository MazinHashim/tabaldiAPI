package com.tabaldi.api.repository;

import com.tabaldi.api.model.UserEntity;
import com.tabaldi.api.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUser(UserEntity user);
    @Transactional
    @Modifying
    @Query("update Vendor v set v.isWorking = ?1 where v.vendorId = ?2")
    int toggleWorkingById(boolean isWorking, long vendorId);

    List<Vendor> findByIsWorking(boolean isWorking);
}
