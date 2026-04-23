package com.erp.modules.sales;

import com.erp.core.security.JwtPrincipal;
import com.erp.core.tenant.TenantContext;
import com.erp.core.users.Role;
import com.erp.modules.sales.dto.SalesByOfficerResponse;
import com.erp.modules.sales.dto.SalesOfficerRequest;
import com.erp.modules.sales.dto.SalesOfficerResponse;
import com.erp.modules.sales.mapper.SalesMapper;
import com.erp.shared.exceptions.BusinessException;
import com.erp.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesOfficerRepository salesOfficerRepository;
    private final SalesMapper salesMapper;

    @Transactional(readOnly = true)
    public List<SalesOfficerResponse> listOfficers() {
        UUID orgId = requireOrg();
        return salesOfficerRepository.findByOrgIdOrderByNameAsc(orgId).stream()
                .map(salesMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesOfficerResponse getMyOfficer(JwtPrincipal principal) {
        UUID orgId = requireOrg();
        return salesOfficerRepository.findByOrgIdAndUserId(orgId, principal.userId())
                .map(salesMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("SalesOfficer", principal.userId()));
    }

    @Transactional
    public SalesOfficerResponse createOfficer(SalesOfficerRequest request) {
        UUID orgId = requireOrg();
        SalesOfficer s = salesMapper.toEntity(request);
        s.setOrgId(orgId);
        return salesMapper.toResponse(salesOfficerRepository.save(s));
    }

    @Transactional
    public SalesOfficerResponse updateOfficer(UUID id, SalesOfficerRequest request) {
        SalesOfficer s = getOfficer(id);
        salesMapper.update(s, request);
        return salesMapper.toResponse(salesOfficerRepository.save(s));
    }

    private SalesOfficer getOfficer(UUID id) {
        UUID orgId = requireOrg();
        return salesOfficerRepository.findById(id)
                .filter(x -> x.getOrgId().equals(orgId))
                .orElseThrow(() -> new NotFoundException("SalesOfficer", id));
    }

    /**
     * Sums {@link com.erp.modules.orders.Order} totals for customers assigned to each officer.
     * {@link Role#SALES_OFFICER} only sees their own row (when linked via {@code userId}).
     */
    @Transactional(readOnly = true)
    public List<SalesByOfficerResponse> getSalesByOfficerInPeriod(JwtPrincipal principal, LocalDate from, LocalDate to) {
        UUID orgId = requireOrg();
        if (from == null || to == null) {
            throw new BusinessException("Query parameters 'from' and 'to' are required (yyyy-MM-dd)");
        }
        if (to.isBefore(from)) {
            throw new BusinessException("'to' must be on or after 'from'");
        }
        List<Object[]> rows = salesOfficerRepository.aggregateSalesByOfficerInPeriod(orgId, from, to);
        List<SalesByOfficerResponse> list = rows.stream()
                .map(r -> new SalesByOfficerResponse(
                        (UUID) r[0],
                        (String) r[1],
                        (BigDecimal) r[2],
                        ((Number) r[3]).longValue()
                ))
                .toList();
        if (principal.role() == Role.SALES_OFFICER) {
            return salesOfficerRepository.findByOrgIdAndUserId(orgId, principal.userId())
                    .map(self -> list.stream()
                            .filter(x -> x.officerId().equals(self.getId()))
                            .toList())
                    .orElse(List.of());
        }
        return list;
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
