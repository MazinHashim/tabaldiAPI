package com.tabaldi.api.repository;

import com.tabaldi.api.model.Address;
import com.tabaldi.api.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomer(Customer customer);

    @Query("SELECT COUNT(*) FROM Address WHERE latitude=:lat and longitude=:lng and customer=:customer")
    int existsByLatLongPerCustomer(@Param("customer") Customer customer,
                                   @Param("lat") double lat,
                                   @Param("lng") double lng);

    @Transactional
    @Modifying
    @Query("update Address a set a.selected = ?1 where a.customer = ?2 and a.addressId = ?3")
    void changeAddressSelection(boolean selected, Customer customer, long addressId);

    Optional<Address> findBySelectedAndCustomer(boolean selected, Customer customer);
}
