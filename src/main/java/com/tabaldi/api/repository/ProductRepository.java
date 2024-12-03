package com.tabaldi.api.repository;

import com.tabaldi.api.model.Category;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where p.vendor.vendorId = ?1 and upper(p.name) = upper(?2)")
    Optional<Product> findByVendorAndName(long vendorId, String name);
    @Transactional
    @Modifying
    @Query("update Product p set p.isPublished = ?1 where p.productId = ?2")
    int togglePublishedById(boolean isPublished, long productId);
    List<Product> findByVendor(Vendor vendor);

    List<Product> findByCategory(Category category);

    @Query("select count(p) from Product p where p.category.categoryId = ?1")
    long countByCategory(long categoryId);

    @Query("select count(p) from Product p where p.vendor.vendorId = ?1")
    long countByVendor(long vendorId);

    List<Product> findByVendorAndIsPublishedAndCategory_isPublished(Vendor vendor, boolean b, boolean c);

    Long countByIsPublishedAndVendor_vendorId(boolean b, long vendorId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.arName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameOrArNameContainingIgnoreCase(@Param("keyword") String keyword);

}
