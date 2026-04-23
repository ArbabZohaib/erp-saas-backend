package com.erp.modules.reminders.dto;

import com.erp.modules.reminders.NotificationChannel;
import com.erp.modules.reminders.ReminderTriggerType;
import jakarta.validation.constraints.NotNull;

public record ReminderRuleRequest(
        @NotNull ReminderTriggerType triggerType,
        @NotNull Integer daysOffset,
        @NotNull NotificationChannel channel
) {
}
