package com.tabaldi.api.repository;

import com.tabaldi.api.model.Option;
import com.tabaldi.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByProduct(Product product);

    @Query("select o from Option o where o.product.productId = ?1 and upper(o.name) = upper(?2)")
    Optional<Option> findByProductAndName(long productId, String name);

}
