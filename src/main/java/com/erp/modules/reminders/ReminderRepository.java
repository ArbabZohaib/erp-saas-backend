package com.erp.modules.reminders;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByOrgIdOrderByScheduledAtDesc(UUID orgId);

    boolean existsByOrgIdAndOrderIdAndReminderRuleIdAndScheduledAt(
            UUID orgId,
            UUID orderId,
            UUID reminderRuleId,
            Instant scheduledAt
    );

    Optional<Reminder> findByOrgIdAndOrderIdAndTypeAndScheduledAt(
            UUID orgId,
            UUID orderId,
            ReminderType type,
            Instant scheduledAt
    );
}
