package com.erp.modules.hr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryRepository extends JpaRepository<Salary, UUID> {

    List<Salary> findByOrgIdOrderByPeriodMonthDesc(UUID orgId);

    Optional<Salary> findByOrgIdAndEmployeeUserIdAndPeriodMonth(UUID orgId, UUID employeeUserId, LocalDate periodMonth);

    List<Salary> findByOrgIdAndEmployeeUserIdOrderByPeriodMonthDesc(UUID orgId, UUID employeeUserId);
}
