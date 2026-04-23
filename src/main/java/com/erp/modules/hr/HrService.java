package com.erp.modules.hr;

import com.erp.core.tenant.TenantContext;
import com.erp.modules.hr.dto.CompensationPlanRequest;
import com.erp.modules.hr.dto.CompensationPlanResponse;
import com.erp.modules.hr.dto.LeaveRequestDto;
import com.erp.modules.hr.dto.LeaveResponse;
import com.erp.modules.hr.dto.SalaryCalculationRequest;
import com.erp.modules.hr.dto.SalaryCalculationResponse;
import com.erp.modules.hr.dto.SalaryFinalizeBatchFailure;
import com.erp.modules.hr.dto.SalaryFinalizeBatchRequest;
import com.erp.modules.hr.dto.SalaryFinalizeBatchResponse;
import com.erp.modules.hr.dto.SalaryFinalizeBatchSuccess;
import com.erp.modules.hr.dto.SalaryRequest;
import com.erp.modules.hr.dto.SalaryResponse;
import com.erp.modules.hr.mapper.HrMapper;
import com.erp.modules.orders.OrderRepository;
import com.erp.shared.exceptions.BusinessException;
import com.erp.shared.exceptions.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HrService {

    private final LeaveRepository leaveRepository;
    private final SalaryRepository salaryRepository;
    private final CompensationPlanRepository compensationPlanRepository;
    private final OrderRepository orderRepository;
    private final HrMapper hrMapper;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;
    private TransactionTemplate requiresNewTx;

    @PostConstruct
    void initRequiresNewTemplate() {
        requiresNewTx = new TransactionTemplate(transactionManager);
        requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> listLeaves() {
        UUID orgId = requireOrg();
        return leaveRepository.findByOrgIdOrderByStartDateDesc(orgId).stream()
                .map(hrMapper::toLeaveResponse)
                .toList();
    }

    @Transactional
    public LeaveResponse createLeave(LeaveRequestDto dto) {
        UUID orgId = requireOrg();
        LeaveRequestEntity e = hrMapper.toLeaveEntity(dto);
        e.setOrgId(orgId);
        e.setStatus(LeaveStatus.PENDING);
        return hrMapper.toLeaveResponse(leaveRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<SalaryResponse> listSalaries(UUID employeeUserId) {
        UUID orgId = requireOrg();
        List<Salary> rows = employeeUserId == null
                ? salaryRepository.findByOrgIdOrderByPeriodMonthDesc(orgId)
                : salaryRepository.findByOrgIdAndEmployeeUserIdOrderByPeriodMonthDesc(orgId, employeeUserId);
        return rows.stream()
                .map(hrMapper::toSalaryResponse)
                .toList();
    }

    @Transactional
    public SalaryResponse createSalary(SalaryRequest request) {
        UUID orgId = requireOrg();
        LocalDate period = monthStart(request.periodMonth());
        if (salaryRepository.findByOrgIdAndEmployeeUserIdAndPeriodMonth(orgId, request.employeeUserId(), period)
                .isPresent()) {
            throw new BusinessException("A salary record already exists for this employee and month.");
        }
        BigDecimal total = scale(request.amount());
        Salary s = hrMapper.toSalaryEntity(request);
        s.setOrgId(orgId);
        s.setPeriodMonth(period);
        s.setBaseAmount(total);
        s.setTargetAmount(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        s.setAchievedAmount(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        s.setIncentiveAmount(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        s.setAmount(total);
        s.setStatus(SalaryStatus.FINALIZED);
        s.setPlanType(CompensationPlanType.MANUAL_ADJUSTMENT);
        s.setPlanSnapshotJson(buildManualSnapshot(request.employeeUserId(), period, total, request.adjustmentNote()));
        return hrMapper.toSalaryResponse(salaryRepository.save(s));
    }

    @Transactional
    public SalaryResponse updateSalary(UUID id, SalaryRequest request) {
        UUID orgId = requireOrg();
        Salary s = getSalary(id);
        if (s.getPlanType() != CompensationPlanType.MANUAL_ADJUSTMENT) {
            throw new BusinessException(
                    "Only manual payout entries can be edited here. Use payroll finalize to change calculated pay.");
        }
        LocalDate period = monthStart(request.periodMonth());
        if (!s.getEmployeeUserId().equals(request.employeeUserId()) || !monthStart(s.getPeriodMonth()).equals(period)) {
            if (salaryRepository
                    .findByOrgIdAndEmployeeUserIdAndPeriodMonth(orgId, request.employeeUserId(), period)
                    .filter(row -> !row.getId().equals(id))
                    .isPresent()) {
                throw new BusinessException("A salary record already exists for this employee and month.");
            }
        }
        hrMapper.updateSalary(s, request);
        s.setPeriodMonth(period);
        BigDecimal total = scale(request.amount());
        s.setBaseAmount(total);
        s.setAmount(total);
        s.setPlanSnapshotJson(buildManualSnapshot(request.employeeUserId(), period, total, request.adjustmentNote()));
        return hrMapper.toSalaryResponse(salaryRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<CompensationPlanResponse> listCompPlans() {
        UUID orgId = requireOrg();
        return compensationPlanRepository.findByOrgIdOrderByUpdatedAtDesc(orgId).stream()
                .map(this::toCompPlanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompensationPlanResponse getCompPlanForEmployee(UUID employeeUserId) {
        UUID orgId = requireOrg();
        return compensationPlanRepository.findByOrgIdAndEmployeeUserId(orgId, employeeUserId)
                .map(this::toCompPlanResponse)
                .orElseThrow(() -> new NotFoundException("No compensation plan for this employee"));
    }

    @Transactional
    public CompensationPlanResponse upsertCompPlan(CompensationPlanRequest request) {
        if (request.planType() == CompensationPlanType.MANUAL_ADJUSTMENT) {
            throw new BusinessException("MANUAL_ADJUSTMENT applies to payout records only, not compensation plans.");
        }
        UUID orgId = requireOrg();
        CompensationPlan plan = compensationPlanRepository
                .findByOrgIdAndEmployeeUserId(orgId, request.employeeUserId())
                .orElseGet(CompensationPlan::new);
        plan.setOrgId(orgId);
        plan.setEmployeeUserId(request.employeeUserId());
        plan.setPlanType(request.planType());
        plan.setBaseSalary(scale(request.baseSalary()));
        if (request.planType() == CompensationPlanType.FIXED_BASE) {
            plan.setTargetAmount(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            plan.setIncentivePercent(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        } else {
            plan.setTargetAmount(scale(request.targetAmount()));
            plan.setIncentivePercent(scalePct(request.incentivePercent()));
        }
        plan.setActive(request.active());
        return toCompPlanResponse(compensationPlanRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public SalaryCalculationResponse calculateSalary(SalaryCalculationRequest request) {
        UUID orgId = requireOrg();
        LocalDate period = monthStart(request.periodMonth());
        CompensationPlan plan = resolvePlan(orgId, request.employeeUserId());
        return calculate(orgId, plan, request.employeeUserId(), period, null);
    }

    @Transactional
    public SalaryCalculationResponse finalizeSalary(SalaryCalculationRequest request) {
        UUID orgId = requireOrg();
        LocalDate period = monthStart(request.periodMonth());
        Salary existing = salaryRepository
                .findByOrgIdAndEmployeeUserIdAndPeriodMonth(orgId, request.employeeUserId(), period)
                .orElse(null);
        boolean replace = Boolean.TRUE.equals(request.replaceFinalized());
        if (existing != null && existing.getStatus() == SalaryStatus.FINALIZED && !replace) {
            throw new BusinessException(
                    "This period is already finalized. Pass replaceFinalized=true to overwrite (including replacing a manual payout with a calculated run).");
        }
        CompensationPlan plan = resolvePlan(orgId, request.employeeUserId());
        SalaryCalculationResponse calc = calculate(orgId, plan, request.employeeUserId(), period, existing);
        String snapshotJson = buildSnapshotJson(plan, calc);
        Salary s = existing != null ? existing : new Salary();
        s.setOrgId(orgId);
        s.setEmployeeUserId(request.employeeUserId());
        s.setPeriodMonth(period);
        s.setBaseAmount(calc.baseAmount());
        s.setTargetAmount(calc.targetAmount());
        s.setAchievedAmount(calc.achievedAmount());
        s.setIncentiveAmount(calc.incentiveAmount());
        s.setAmount(calc.totalAmount());
        s.setStatus(SalaryStatus.FINALIZED);
        s.setPlanType(plan.getPlanType());
        s.setPlanSnapshotJson(snapshotJson);
        Salary saved = salaryRepository.save(s);
        boolean targetMet = plan.getPlanType() != CompensationPlanType.TARGET_INCENTIVE
                || saved.getAchievedAmount().compareTo(saved.getTargetAmount()) >= 0;
        return new SalaryCalculationResponse(
                saved.getId(),
                saved.getEmployeeUserId(),
                saved.getPeriodMonth(),
                saved.getBaseAmount(),
                saved.getTargetAmount(),
                saved.getAchievedAmount(),
                plan.getIncentivePercent(),
                saved.getIncentiveAmount(),
                saved.getAmount(),
                targetMet,
                saved.getStatus(),
                plan.getPlanType(),
                saved.getPlanSnapshotJson()
        );
    }

    /**
     * Finalizes payroll for many employees in one API call. Each employee runs in {@link TransactionDefinition#PROPAGATION_REQUIRES_NEW}
     * so one failure does not roll back the others.
     */
    public SalaryFinalizeBatchResponse finalizeSalaryBatch(SalaryFinalizeBatchRequest request) {
        UUID orgId = requireOrg();
        LocalDate period = monthStart(request.periodMonth());
        boolean replace = Boolean.TRUE.equals(request.replaceFinalized());
        List<UUID> employees;
        List<UUID> requested = request.employeeUserIds();
        if (requested != null && !requested.isEmpty()) {
            employees = List.copyOf(requested);
        } else {
            employees = compensationPlanRepository.findByOrgIdAndActiveTrueOrderByEmployeeUserIdAsc(orgId).stream()
                    .map(CompensationPlan::getEmployeeUserId)
                    .distinct()
                    .toList();
        }
        List<SalaryFinalizeBatchSuccess> successes = new ArrayList<>();
        List<SalaryFinalizeBatchFailure> failures = new ArrayList<>();
        for (UUID emp : employees) {
            try {
                SalaryCalculationResponse r = requiresNewTx.execute(
                        status -> finalizeSalary(new SalaryCalculationRequest(emp, period, replace)));
                successes.add(new SalaryFinalizeBatchSuccess(emp, r.salaryId(), r.totalAmount()));
            } catch (RuntimeException e) {
                failures.add(new SalaryFinalizeBatchFailure(emp, batchErrorMessage(e)));
            }
        }
        return new SalaryFinalizeBatchResponse(period, successes.size(), failures.size(), successes, failures);
    }

    private static String batchErrorMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        String msg = t.getMessage();
        return msg != null && !msg.isBlank() ? msg : "Finalize failed";
    }

    private SalaryCalculationResponse calculate(
            UUID orgId,
            CompensationPlan plan,
            UUID employeeUserId,
            LocalDate periodMonth,
            Salary existing
    ) {
        LocalDate from = periodMonth;
        LocalDate to = periodMonth.plusMonths(1).minusDays(1);
        BigDecimal achieved = scale(orderRepository.sumForSalesOfficerUserInPeriod(orgId, employeeUserId, from, to));
        BigDecimal base = scale(plan.getBaseSalary());
        if (plan.getPlanType() == CompensationPlanType.FIXED_BASE) {
            return new SalaryCalculationResponse(
                    existing != null ? existing.getId() : null,
                    employeeUserId,
                    periodMonth,
                    base,
                    BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                    achieved,
                    BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                    base,
                    true,
                    existing != null ? existing.getStatus() : SalaryStatus.DRAFT,
                    plan.getPlanType(),
                    null
            );
        }
        BigDecimal target = scale(plan.getTargetAmount());
        BigDecimal pct = scalePct(plan.getIncentivePercent());
        boolean targetMet = achieved.compareTo(target) >= 0;
        BigDecimal incentive = targetMet
                ? scale(achieved.multiply(pct).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        BigDecimal total = scale(base.add(incentive));
        return new SalaryCalculationResponse(
                existing != null ? existing.getId() : null,
                employeeUserId,
                periodMonth,
                base,
                target,
                achieved,
                pct,
                incentive,
                total,
                targetMet,
                existing != null ? existing.getStatus() : SalaryStatus.DRAFT,
                plan.getPlanType(),
                null
        );
    }

    private CompensationPlan resolvePlan(UUID orgId, UUID employeeUserId) {
        CompensationPlan plan = compensationPlanRepository.findByOrgIdAndEmployeeUserId(orgId, employeeUserId)
                .orElseThrow(() -> new NotFoundException("Compensation plan not found for employee: " + employeeUserId));
        if (!plan.isActive()) {
            throw new BusinessException("Compensation plan is inactive for employee: " + employeeUserId);
        }
        return plan;
    }

    private CompensationPlanResponse toCompPlanResponse(CompensationPlan p) {
        return new CompensationPlanResponse(
                p.getId(),
                p.getOrgId(),
                p.getEmployeeUserId(),
                p.getPlanType(),
                p.getBaseSalary(),
                p.getTargetAmount(),
                p.getIncentivePercent(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private String buildManualSnapshot(UUID employeeUserId, LocalDate periodMonth, BigDecimal amount, String note) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("source", CompensationPlanType.MANUAL_ADJUSTMENT.name());
        m.put("employeeUserId", employeeUserId.toString());
        m.put("periodMonth", periodMonth.toString());
        m.put("totalAmount", amount.toPlainString());
        if (note != null && !note.isBlank()) {
            m.put("note", note.trim());
        }
        try {
            return objectMapper.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Could not serialize manual payout snapshot");
        }
    }

    private String buildSnapshotJson(CompensationPlan plan, SalaryCalculationResponse calc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("planType", plan.getPlanType().name());
        m.put("periodMonth", calc.periodMonth().toString());
        m.put("baseAmount", calc.baseAmount().toPlainString());
        m.put("targetAmount", calc.targetAmount().toPlainString());
        m.put("achievedAmount", calc.achievedAmount().toPlainString());
        m.put("incentivePercent", plan.getIncentivePercent().toPlainString());
        m.put("incentiveAmount", calc.incentiveAmount().toPlainString());
        m.put("totalAmount", calc.totalAmount().toPlainString());
        m.put("targetMet", calc.targetMet());
        m.put("formula", plan.getPlanType() == CompensationPlanType.FIXED_BASE
                ? "total = base"
                : "incentive = achieved * (incentivePercent/100) when achieved >= target; total = base + incentive");
        try {
            return objectMapper.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Could not serialize payroll snapshot");
        }
    }

    private Salary getSalary(UUID id) {
        UUID orgId = requireOrg();
        return salaryRepository.findById(id)
                .filter(x -> x.getOrgId().equals(orgId))
                .orElseThrow(() -> new NotFoundException("Salary", id));
    }

    private static LocalDate monthStart(LocalDate d) {
        return d.withDayOfMonth(1);
    }

    private static BigDecimal scale(BigDecimal v) {
        BigDecimal n = v != null ? v : BigDecimal.ZERO;
        return n.setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal scalePct(BigDecimal v) {
        BigDecimal n = scale(v);
        if (n.signum() < 0) {
            throw new BusinessException("Incentive percent cannot be negative");
        }
        return n;
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
