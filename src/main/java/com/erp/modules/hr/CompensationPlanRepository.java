package com.erp.modules.hr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompensationPlanRepository extends JpaRepository<CompensationPlan, UUID> {

    List<CompensationPlan> findByOrgIdOrderByUpdatedAtDesc(UUID orgId);

    List<CompensationPlan> findByOrgIdAndActiveTrueOrderByEmployeeUserIdAsc(UUID orgId);

    Optional<CompensationPlan> findByOrgIdAndEmployeeUserId(UUID orgId, UUID employeeUserId);
}
