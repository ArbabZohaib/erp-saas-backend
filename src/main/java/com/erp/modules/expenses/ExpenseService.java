package com.erp.modules.expenses;

import com.erp.core.tenant.TenantContext;
import com.erp.modules.expenses.dto.ExpenseRequest;
import com.erp.modules.expenses.dto.ExpenseResponse;
import com.erp.modules.expenses.mapper.ExpenseMapper;
import com.erp.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list() {
        UUID orgId = requireOrg();
        return expenseRepository.findByOrgIdOrderByDateDesc(orgId).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        UUID orgId = requireOrg();
        Expense e = expenseMapper.toEntity(request);
        e.setOrgId(orgId);
        return expenseMapper.toResponse(expenseRepository.save(e));
    }

    @Transactional
    public ExpenseResponse update(UUID id, ExpenseRequest request) {
        Expense e = getEntity(id);
        expenseMapper.update(e, request);
        return expenseMapper.toResponse(expenseRepository.save(e));
    }

    private Expense getEntity(UUID id) {
        UUID orgId = requireOrg();
        return expenseRepository.findById(id)
                .filter(x -> x.getOrgId().equals(orgId))
                .orElseThrow(() -> new NotFoundException("Expense", id));
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
