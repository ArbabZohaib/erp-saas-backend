package com.erp.modules.hr;

import com.erp.modules.hr.dto.LeaveRequestDto;
import com.erp.modules.hr.dto.LeaveResponse;
import com.erp.modules.hr.dto.CompensationPlanRequest;
import com.erp.modules.hr.dto.CompensationPlanResponse;
import com.erp.modules.hr.dto.SalaryCalculationRequest;
import com.erp.modules.hr.dto.SalaryCalculationResponse;
import com.erp.modules.hr.dto.SalaryFinalizeBatchRequest;
import com.erp.modules.hr.dto.SalaryFinalizeBatchResponse;
import com.erp.modules.hr.dto.SalaryRequest;
import com.erp.modules.hr.dto.SalaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
public class HrController {

    private final HrService hrService;

    @GetMapping("/leaves")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<LeaveResponse> listLeaves() {
        return hrService.listLeaves();
    }

    @PostMapping("/leaves")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public LeaveResponse createLeave(@Valid @RequestBody LeaveRequestDto dto) {
        return hrService.createLeave(dto);
    }

    @GetMapping("/salaries")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SalaryResponse> listSalaries(@RequestParam(required = false) UUID employeeUserId) {
        return hrService.listSalaries(employeeUserId);
    }

    @PostMapping("/salaries")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SalaryResponse createSalary(@Valid @RequestBody SalaryRequest request) {
        return hrService.createSalary(request);
    }

    @PutMapping("/salaries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SalaryResponse updateSalary(@PathVariable UUID id, @Valid @RequestBody SalaryRequest request) {
        return hrService.updateSalary(id, request);
    }

    @GetMapping("/compensation-plans")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CompensationPlanResponse> listCompensationPlans() {
        return hrService.listCompPlans();
    }

    @GetMapping("/compensation-plans/employee/{employeeUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public CompensationPlanResponse getCompensationPlanForEmployee(@PathVariable UUID employeeUserId) {
        return hrService.getCompPlanForEmployee(employeeUserId);
    }

    @PostMapping("/compensation-plans")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CompensationPlanResponse upsertCompensationPlan(@Valid @RequestBody CompensationPlanRequest request) {
        return hrService.upsertCompPlan(request);
    }

    @PostMapping("/salaries/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public SalaryCalculationResponse calculateSalary(@Valid @RequestBody SalaryCalculationRequest request) {
        return hrService.calculateSalary(request);
    }

    @PostMapping("/salaries/finalize")
    @PreAuthorize("hasRole('ADMIN')")
    public SalaryCalculationResponse finalizeSalary(@Valid @RequestBody SalaryCalculationRequest request) {
        return hrService.finalizeSalary(request);
    }

    @PostMapping("/salaries/finalize-batch")
    @PreAuthorize("hasRole('ADMIN')")
    public SalaryFinalizeBatchResponse finalizeSalaryBatch(@Valid @RequestBody SalaryFinalizeBatchRequest request) {
        return hrService.finalizeSalaryBatch(request);
    }
}
