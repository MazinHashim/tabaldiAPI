package com.tabaldi.api.repository;

import com.tabaldi.api.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {

//    @Query("from UserVerification v1, UserVerification v2 where v1.phone=:phone" +
//            " and v1.createdTime = MAX(v2.createdTime) group by v1.createdTime")
    @Query("FROM UserVerification WHERE phone = :phone ORDER BY createdTime DESC LIMIT 1")
    Optional<UserVerification> findLastSentCode(@Param("phone") String phone);

    Optional<UserVerification> findByPhoneAndCodeAndKeyRef(String phone, int otpCode, String keyRef);
}
