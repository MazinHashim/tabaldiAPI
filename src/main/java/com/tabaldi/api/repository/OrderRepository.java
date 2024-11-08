package com.tabaldi.api.repository;

import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.model.OrderStatus;
import com.tabaldi.api.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = "select count(*) from orders where DATE(order_date) = CURRENT_DATE", nativeQuery = true)
    long countByOrderDate();
    @Query("select o from Order o where " +
            "o.orderId in (select inv.order.orderId from Invoice inv)")
    List<Order>  findAllHasInvoice();
    @Query("select o from Order o where o.customer.customerId = ?1 and o.vendor.vendorId = ?2 and " +
            "o.status not in ('DELIVERED', 'CANCELED') and o.orderId in (select inv.order.orderId from Invoice inv) " +
            "ORDER BY o.orderDate DESC")
    List<Order> getLastActiveOrderPerVendor(long customerId, long vendorId);

    @Query("select o from Order o where o.status not in ?1 and " +
            "o.orderId in (select inv.order.orderId from Invoice inv)")
    List<Order> findByPendingOrders(Collection<OrderStatus> statuses);
    @Query("select o from Order o where o.status not in ?1 and o.customer.customerId=?2 " +
            "and o.orderId in (select inv.order.orderId from Invoice inv)")
    List<Order> findPendingOrdersByCustomer(Collection<OrderStatus> statuses, long customerId);
    @Query("select o from Order o where o.vendor = ?1 " +
            "and o.orderId in (select inv.order.orderId from Invoice inv)")
    List<Order> findByVendor(Vendor vendor);
    @Query("select o from Order o where o.customer = ?1 " +
            "and o.orderId in (select inv.order.orderId from Invoice inv)")
    List<Order> findByCustomer(Customer customer);
}
