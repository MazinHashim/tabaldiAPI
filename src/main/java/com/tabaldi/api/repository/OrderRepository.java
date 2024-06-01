package com.tabaldi.api.repository;

import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.model.OrderStatus;
import com.tabaldi.api.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = "select count(*) from orders where DATE(order_date) = CURRENT_DATE", nativeQuery = true)
    long countByOrderDate();
    @Query("select o from Order o where o.customer.customerId = ?1 and o.vendor.vendorId = ?2 and " +
            "o.status not in ('DELIVERED', 'CANCELED') ORDER BY o.orderDate DESC LIMIT 1")
    Optional<Order> getLastActiveOrderPerVendor(long customerId, long vendorId);

    List<Order> findByVendor(Vendor vendor);

    List<Order> findByCustomer(Customer customer);
}
