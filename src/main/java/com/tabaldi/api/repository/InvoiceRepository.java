package com.tabaldi.api.repository;

import com.tabaldi.api.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT inv FROM Invoice inv WHERE inv.order.vendor.vendorId=:vendorId")
    List<Invoice> findByVendorId(@Param("vendorId") long vendorId);
    @Query("SELECT inv FROM Invoice inv WHERE inv.order.orderId=:orderId")
    Optional<Invoice> findByOrderId(@Param("orderId") long orderId);

    void deleteByOrder_cartItems_product_productId(long productId);
}
