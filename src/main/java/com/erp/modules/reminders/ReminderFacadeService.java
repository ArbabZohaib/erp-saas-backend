package com.erp.modules.reminders;

import com.erp.core.tenant.TenantContext;
import com.erp.modules.reminders.dto.ReminderResponse;
import com.erp.modules.reminders.dto.ReminderRuleRequest;
import com.erp.modules.reminders.dto.ReminderRuleResponse;
import com.erp.modules.reminders.mapper.ReminderMapper;
import com.erp.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReminderFacadeService {

    private final ReminderRepository reminderRepository;
    private final ReminderRuleRepository reminderRuleRepository;
    private final ReminderMapper reminderMapper;

    @Transactional(readOnly = true)
    public List<ReminderResponse> listReminders() {
        UUID orgId = requireOrg();
        return reminderRepository.findByOrgIdOrderByScheduledAtDesc(orgId).stream()
                .map(reminderMapper::toReminderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderRuleResponse> listRules() {
        UUID orgId = requireOrg();
        return reminderRuleRepository.findByOrgId(orgId).stream()
                .map(reminderMapper::toRuleResponse)
                .toList();
    }

    @Transactional
    public ReminderRuleResponse createRule(ReminderRuleRequest request) {
        UUID orgId = requireOrg();
        ReminderRule rule = new ReminderRule();
        rule.setOrgId(orgId);
        rule.setTriggerType(request.triggerType());
        rule.setDaysOffset(request.daysOffset());
        rule.setChannel(request.channel());
        return reminderMapper.toRuleResponse(reminderRuleRepository.save(rule));
    }

    @Transactional
    public ReminderRuleResponse updateRule(UUID id, ReminderRuleRequest request) {
        ReminderRule rule = getRule(id);
        rule.setTriggerType(request.triggerType());
        rule.setDaysOffset(request.daysOffset());
        rule.setChannel(request.channel());
        return reminderMapper.toRuleResponse(reminderRuleRepository.save(rule));
    }

    private ReminderRule getRule(UUID id) {
        UUID orgId = requireOrg();
        return reminderRuleRepository.findById(id)
                .filter(r -> r.getOrgId().equals(orgId))
                .orElseThrow(() -> new NotFoundException("ReminderRule", id));
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
