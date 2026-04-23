package com.erp.modules.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerformanceRepository extends JpaRepository<Performance, UUID> {

    Optional<Performance> findByOrgIdAndSalesOfficerId(UUID orgId, UUID salesOfficerId);
}
