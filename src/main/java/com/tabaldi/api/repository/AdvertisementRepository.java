package com.tabaldi.api.repository;

import com.tabaldi.api.model.Advertisement;
import com.tabaldi.api.model.Vendor;
import com.tabaldi.api.model.VendorType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByVendor(Vendor vendor);

    @Transactional
    @Modifying
    @Query("update Advertisement ads set ads.isShown = ?1 where ads.advertisementId = ?2")
    int toggleShownById(boolean isShown, long advertisementId);

    List<Advertisement> findByIsShownAndExpireDateGreaterThan(boolean b, LocalDate now);

    List<Advertisement> findByVendorIsNull();

    int deleteByPriorityAndVendorIsNull(int priority);

    int deleteByPriorityAndVendorVendorType(int priority, VendorType vendorType);

    List<Advertisement> findByVendorVendorType(VendorType vendorType);
}
