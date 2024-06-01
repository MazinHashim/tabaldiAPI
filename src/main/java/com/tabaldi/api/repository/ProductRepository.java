package com.tabaldi.api.repository;

import com.tabaldi.api.model.Category;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where p.vendor.vendorId = ?1 and upper(p.name) = upper(?2)")
    Optional<Product> findByVendorAndName(long vendorId, String name);

    List<Product> findByVendor(Vendor vendor);

    List<Product> findByCategory(Category category);

    @Query("select count(p) from Product p where p.category.categoryId = ?1")
    long countByCategory(long categoryId);

    @Query("select count(p) from Product p where p.vendor.vendorId = ?1")
    long countByVendor(long vendorId);

}
