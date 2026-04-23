package com.erp.modules.reminders;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReminderRuleRepository extends JpaRepository<ReminderRule, UUID> {

    List<ReminderRule> findByOrgId(UUID orgId);
}
