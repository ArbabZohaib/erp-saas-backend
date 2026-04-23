package com.erp.modules.analytics;

import com.erp.core.security.JwtPrincipal;
import com.erp.modules.analytics.dto.SalesTimeseriesResponse;
import com.erp.shared.exceptions.BusinessException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Order totals by calendar month or year, optionally filtered by customer and/or sales officer.
     * Sales officers only see their own slice; filters use each order's order date.
     */
    @GetMapping("/sales-timeseries")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public SalesTimeseriesResponse salesTimeseries(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "MONTH") String groupBy,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID salesOfficerId
    ) {
        String gNorm = groupBy == null ? "MONTH" : groupBy.trim().toUpperCase();
        AnalyticsService.GroupBy g = switch (gNorm) {
            case "MONTH" -> AnalyticsService.GroupBy.MONTH;
            case "YEAR" -> AnalyticsService.GroupBy.YEAR;
            default -> throw new BusinessException("groupBy must be MONTH or YEAR");
        };
        return analyticsService.salesTimeseries(principal, from, to, g, customerId, salesOfficerId);
    }
}
