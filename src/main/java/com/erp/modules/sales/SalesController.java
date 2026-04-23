package com.erp.modules.sales;

import com.erp.core.security.JwtPrincipal;
import com.erp.modules.sales.dto.SalesByOfficerResponse;
import com.erp.modules.sales.dto.SalesOfficerRequest;
import com.erp.modules.sales.dto.SalesOfficerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @GetMapping("/officers")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<SalesOfficerResponse> listOfficers() {
        return salesService.listOfficers();
    }

    /** Current user's linked sales officer row (for dashboards / analytics filters). */
    @GetMapping("/officers/me")
    @PreAuthorize("hasRole('SALES_OFFICER')")
    public SalesOfficerResponse myOfficer(@AuthenticationPrincipal JwtPrincipal principal) {
        return salesService.getMyOfficer(principal);
    }

    /**
     * Order totals for customers assigned to each sales officer, filtered by order date range (inclusive).
     * Sales officers only receive their own aggregate (when their user is linked to an officer row).
     */
    @GetMapping("/by-officer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public List<SalesByOfficerResponse> salesByOfficer(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return salesService.getSalesByOfficerInPeriod(principal, from, to);
    }

    @PostMapping("/officers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SalesOfficerResponse createOfficer(@Valid @RequestBody SalesOfficerRequest request) {
        return salesService.createOfficer(request);
    }

    @PutMapping("/officers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SalesOfficerResponse updateOfficer(@PathVariable UUID id, @Valid @RequestBody SalesOfficerRequest request) {
        return salesService.updateOfficer(id, request);
    }
}
