package com.tabaldi.api.repository;

import com.tabaldi.api.model.InvoiceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceSummaryRepository extends JpaRepository<InvoiceSummary, Long> {

}
