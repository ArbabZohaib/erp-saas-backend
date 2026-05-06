package com.erp.modules.expenses;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseInvoiceAttachmentRepository extends JpaRepository<ExpenseInvoiceAttachment, UUID> {

    Optional<ExpenseInvoiceAttachment> findFirstByOrgIdAndExpenseIdOrderByCreatedAtDesc(UUID orgId, UUID expenseId);

    boolean existsByOrgIdAndExpenseId(UUID orgId, UUID expenseId);
}
