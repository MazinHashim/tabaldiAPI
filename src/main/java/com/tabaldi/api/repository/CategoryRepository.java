package com.tabaldi.api.repository;

import com.tabaldi.api.model.Category;
import com.tabaldi.api.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Transactional
    @Modifying
    @Query("update Category c set c.isPublished = ?1 where c.categoryId = ?2")
    int togglePublishedById(boolean isPublished, long categoryId);
    List<Category> findByVendor(Vendor vendor);

    @Query("select c from Category c where c.vendor.vendorId = ?1 and upper(c.name) = upper(?2)")
    Optional<Category> findByVendorAndName(long vendorId, String name);

    Long countByIsPublishedAndVendor_vendorId(boolean b, long vendorId);
}
