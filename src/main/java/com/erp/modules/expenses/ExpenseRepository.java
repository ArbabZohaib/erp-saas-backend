package com.erp.modules.expenses;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByOrgIdOrderByDateDesc(UUID orgId);
}
