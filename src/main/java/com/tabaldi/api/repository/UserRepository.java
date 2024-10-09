package com.tabaldi.api.repository;

import com.tabaldi.api.model.Role;
import com.tabaldi.api.model.UserEntity;
import com.tabaldi.api.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByPhone(String phone);
    List<UserEntity> findByVendorAndRole(Vendor vendor, Role role);
    Optional<UserEntity> findByEmail(String email);
}
