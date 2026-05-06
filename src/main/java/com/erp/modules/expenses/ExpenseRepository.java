package com.erp.modules.expenses;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByOrgIdOrderByDateDesc(UUID orgId);

    List<Expense> findByOrgIdAndUserIdOrderByDateDesc(UUID orgId, UUID userId);

    List<Expense> findByOrgIdAndDateBetweenOrderByDateDesc(UUID orgId, LocalDate startDate, LocalDate endDate);

    List<Expense> findByOrgIdAndUserIdAndDateBetweenOrderByDateDesc(UUID orgId, UUID userId, LocalDate startDate, LocalDate endDate);
}
