package com.tabaldi.api.repository;

import com.tabaldi.api.model.CartItem;
import com.tabaldi.api.model.Customer;
import com.tabaldi.api.model.Order;
import com.tabaldi.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByProduct(Product product);

    List<CartItem> findByCustomer(Customer customer);

    List<CartItem> findByOrder(Order order);

    List<CartItem> findByCustomerAndOrderIsNull(Customer customer);
}
