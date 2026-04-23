package com.erp.modules.reminders.dto;

import com.erp.modules.reminders.ReminderStatus;
import com.erp.modules.reminders.ReminderType;

import java.time.Instant;
import java.util.UUID;

public record ReminderResponse(
        UUID id,
        UUID orgId,
        UUID customerId,
        UUID orderId,
        ReminderType type,
        Instant scheduledAt,
        ReminderStatus status,
        UUID reminderRuleId,
        Instant createdAt,
        Instant updatedAt
) {
}
