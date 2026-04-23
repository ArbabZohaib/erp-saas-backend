package com.erp.modules.analytics;

import com.erp.core.security.JwtPrincipal;
import com.erp.core.tenant.TenantContext;
import com.erp.core.users.Role;
import com.erp.modules.analytics.dto.SalesBucket;
import com.erp.modules.analytics.dto.SalesTimeseriesResponse;
import com.erp.modules.customers.Customer;
import com.erp.modules.customers.CustomerRepository;
import com.erp.modules.customers.api.CustomerLookup;
import com.erp.modules.orders.Order;
import com.erp.modules.orders.OrderRepository;
import com.erp.modules.sales.SalesOfficerRepository;
import com.erp.shared.exceptions.BusinessException;
import com.erp.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    public enum GroupBy {
        MONTH,
        YEAR
    }

    private final OrderRepository orderRepository;
    private final CustomerLookup customerLookup;
    private final CustomerRepository customerRepository;
    private final SalesOfficerRepository salesOfficerRepository;

    @Transactional(readOnly = true)
    public SalesTimeseriesResponse salesTimeseries(
            JwtPrincipal principal,
            LocalDate from,
            LocalDate to,
            GroupBy groupBy,
            UUID customerId,
            UUID salesOfficerId
    ) {
        UUID orgId = requireOrg();
        if (from == null || to == null) {
            throw new BusinessException("Query parameters 'from' and 'to' are required (yyyy-MM-dd)");
        }
        if (to.isBefore(from)) {
            throw new BusinessException("'to' must be on or after 'from'");
        }

        if (customerId != null) {
            customerLookup.assertExists(orgId, customerId);
        }
        if (salesOfficerId != null) {
            salesOfficerRepository.findById(salesOfficerId)
                    .filter(o -> o.getOrgId().equals(orgId))
                    .orElseThrow(() -> new NotFoundException("SalesOfficer", salesOfficerId));
        }

        UUID effectiveOfficerId = salesOfficerId;
        if (principal.role() == Role.SALES_OFFICER) {
            var self = salesOfficerRepository.findByOrgIdAndUserId(orgId, principal.userId())
                    .orElseThrow(() -> new BusinessException("No sales officer linked to your account"));
            if (effectiveOfficerId != null && !effectiveOfficerId.equals(self.getId())) {
                throw new BusinessException("You can only view analytics for your own sales");
            }
            effectiveOfficerId = self.getId();
            if (customerId != null) {
                Customer c = customerRepository.findByIdAndOrgId(customerId, orgId)
                        .orElseThrow(() -> new NotFoundException("Customer", customerId));
                if (c.getAssignedSalesOfficerId() == null
                        || !c.getAssignedSalesOfficerId().equals(effectiveOfficerId)) {
                    throw new BusinessException("Customer is not assigned to you");
                }
            }
        }

        List<Order> orders = orderRepository.findForAnalytics(orgId, from, to, customerId, effectiveOfficerId);
        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = orders.size();
        List<SalesBucket> buckets = groupBy == GroupBy.MONTH
                ? fillMonthlyBuckets(from, to, orders)
                : fillYearlyBuckets(from, to, orders);

        return new SalesTimeseriesResponse(
                from,
                to,
                groupBy.name(),
                customerId,
                principal.role() == Role.SALES_OFFICER ? effectiveOfficerId : salesOfficerId,
                buckets,
                totalSales,
                totalOrders
        );
    }

    private static List<SalesBucket> fillMonthlyBuckets(LocalDate from, LocalDate to, List<Order> orders) {
        Map<String, BigDecimal> amounts = new HashMap<>();
        Map<String, Long> counts = new HashMap<>();
        for (Order o : orders) {
            String key = YearMonth.from(o.getOrderDate()).toString();
            amounts.merge(key, o.getTotalAmount(), BigDecimal::add);
            counts.merge(key, 1L, Long::sum);
        }
        YearMonth start = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);
        List<SalesBucket> out = new ArrayList<>();
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            String key = ym.toString();
            out.add(new SalesBucket(
                    key,
                    amounts.getOrDefault(key, BigDecimal.ZERO),
                    counts.getOrDefault(key, 0L)
            ));
        }
        return out;
    }

    private static List<SalesBucket> fillYearlyBuckets(LocalDate from, LocalDate to, List<Order> orders) {
        Map<String, BigDecimal> amounts = new HashMap<>();
        Map<String, Long> counts = new HashMap<>();
        for (Order o : orders) {
            String key = String.format("%04d", o.getOrderDate().getYear());
            amounts.merge(key, o.getTotalAmount(), BigDecimal::add);
            counts.merge(key, 1L, Long::sum);
        }
        List<SalesBucket> out = new ArrayList<>();
        for (int y = from.getYear(); y <= to.getYear(); y++) {
            String key = String.format("%04d", y);
            out.add(new SalesBucket(
                    key,
                    amounts.getOrDefault(key, BigDecimal.ZERO),
                    counts.getOrDefault(key, 0L)
            ));
        }
        return out;
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
