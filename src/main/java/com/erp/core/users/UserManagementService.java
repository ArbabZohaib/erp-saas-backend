package com.erp.core.users;

import com.erp.core.auth.PasswordResetTokenRepository;
import com.erp.core.module.ModuleCode;
import com.erp.core.organization.OrganizationService;
import com.erp.core.tenant.TenantContext;
import com.erp.core.users.dto.CompensationOnboardingRequest;
import com.erp.core.users.dto.CreateUserRequest;
import com.erp.core.users.dto.UserResponse;
import com.erp.modules.hr.CompensationPlan;
import com.erp.modules.hr.CompensationPlanRepository;
import com.erp.modules.hr.CompensationPlanType;
import com.erp.modules.sales.SalesOfficer;
import com.erp.modules.sales.SalesOfficerRepository;
import com.erp.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final SalesOfficerRepository salesOfficerRepository;
    private final CompensationPlanRepository compensationPlanRepository;
    private final OrganizationService organizationService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public void adminSetPassword(UUID userId, String newPassword) {
        UUID orgId = requireOrg();
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (!user.getOrgId().equals(orgId)) {
            throw new BusinessException("User not found");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.deleteAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsersInTenant() {
        UUID orgId = requireOrg();
        return userRepository.findByOrgIdOrderByEmailAsc(orgId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UUID orgId = requireOrg();
        String email = request.email().trim().toLowerCase();
        if (userRepository.findByOrgIdAndEmail(orgId, email).isPresent()) {
            throw new BusinessException("User with this email already exists in the organization");
        }
        AppUser user = new AppUser();
        user.setOrgId(orgId);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(true);
        try {
            AppUser saved = userRepository.save(user);
            if (saved.getRole() == Role.SALES_OFFICER) {
                ensureSalesOfficerForUser(orgId, saved.getId(), email);
            }
            if (request.compensation() != null) {
                if (!organizationService.isModuleEnabled(orgId, ModuleCode.HR)) {
                    throw new BusinessException("Compensation is only available when the HR module is enabled");
                }
                applyOnboardingCompensation(orgId, saved.getId(), request.compensation());
            }
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Could not create user (email may already exist)");
        }
    }

    private void applyOnboardingCompensation(UUID orgId, UUID employeeUserId, CompensationOnboardingRequest c) {
        validateOnboarding(c);
        CompensationPlan plan = compensationPlanRepository
                .findByOrgIdAndEmployeeUserId(orgId, employeeUserId)
                .orElseGet(CompensationPlan::new);
        plan.setOrgId(orgId);
        plan.setEmployeeUserId(employeeUserId);
        plan.setPlanType(c.planType());
        plan.setBaseSalary(scaleMoney(c.baseSalary()));
        if (c.planType() == CompensationPlanType.TARGET_INCENTIVE) {
            plan.setTargetAmount(scaleMoney(c.targetAmount()));
            plan.setIncentivePercent(scalePct(c.incentivePercent()));
        } else {
            plan.setTargetAmount(BigDecimal.ZERO);
            plan.setIncentivePercent(BigDecimal.ZERO);
        }
        plan.setActive(true);
        compensationPlanRepository.save(plan);
    }

    private static void validateOnboarding(CompensationOnboardingRequest c) {
        if (c.planType() == CompensationPlanType.MANUAL_ADJUSTMENT) {
            throw new BusinessException("MANUAL_ADJUSTMENT is not a valid plan type for onboarding");
        }
        if (c.baseSalary() == null || c.baseSalary().signum() < 0) {
            throw new BusinessException("Base salary must be zero or positive");
        }
        if (c.planType() == CompensationPlanType.TARGET_INCENTIVE) {
            if (c.targetAmount() == null || c.incentivePercent() == null) {
                throw new BusinessException("Target amount and incentive percent are required for TARGET_INCENTIVE");
            }
            if (c.targetAmount().signum() < 0 || c.incentivePercent().signum() < 0) {
                throw new BusinessException("Target and incentive percent must be non-negative");
            }
        }
    }

    private static BigDecimal scaleMoney(BigDecimal v) {
        return v.setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal scalePct(BigDecimal v) {
        return v.setScale(4, RoundingMode.HALF_UP);
    }

    /** Creates a sales_officers row for new SALES_OFFICER users so CRM and stats stay in sync. */
    private void ensureSalesOfficerForUser(UUID orgId, UUID userId, String email) {
        if (salesOfficerRepository.findByOrgIdAndUserId(orgId, userId).isPresent()) {
            return;
        }
        SalesOfficer officer = new SalesOfficer();
        officer.setOrgId(orgId);
        officer.setUserId(userId);
        officer.setName(officerDisplayNameFromEmail(email));
        salesOfficerRepository.save(officer);
    }

    /** e.g. {@code jane.doe@acme.demo} → {@code Jane Doe}, {@code rahulcool@...} → {@code Rahulcool} */
    private static String officerDisplayNameFromEmail(String email) {
        int at = email.indexOf('@');
        String local = (at > 0 ? email.substring(0, at) : email).trim();
        if (local.isEmpty()) {
            return "Sales officer";
        }
        String[] parts = local.split("[._\\s]+");
        StringJoiner joiner = new StringJoiner(" ");
        for (String p : parts) {
            if (p.isEmpty()) {
                continue;
            }
            joiner.add(p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase());
        }
        String s = joiner.toString();
        return s.isEmpty() ? "Sales officer" : s;
    }

    private UserResponse toResponse(AppUser u) {
        return new UserResponse(
                u.getId(),
                u.getOrgId(),
                u.getEmail(),
                u.getRole(),
                u.isEnabled(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
