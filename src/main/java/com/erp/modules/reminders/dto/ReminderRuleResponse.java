package com.erp.modules.reminders.dto;

import com.erp.modules.reminders.NotificationChannel;
import com.erp.modules.reminders.ReminderTriggerType;

import java.time.Instant;
import java.util.UUID;

public record ReminderRuleResponse(
        UUID id,
        UUID orgId,
        ReminderTriggerType triggerType,
        int daysOffset,
        NotificationChannel channel,
        Instant createdAt,
        Instant updatedAt
) {
}
