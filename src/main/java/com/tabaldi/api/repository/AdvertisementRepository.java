package com.tabaldi.api.repository;

import com.tabaldi.api.model.Advertisement;
import com.tabaldi.api.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByVendor(Vendor vendor);
    @Transactional
    @Modifying
    @Query("update Advertisement ads set ads.isShown = ?1 where ads.advertisementId = ?2")
    int toggleShownById(boolean isShown, long advertisementId);

    List<Advertisement> findByIsShownAndExpireInGreaterThan(boolean b, OffsetDateTime now);
}
