package com.erp.modules.reminders;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reminder_rules")
@Getter
@Setter
public class ReminderRule extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 32)
    private ReminderTriggerType triggerType;

    @Column(name = "days_offset", nullable = false)
    private int daysOffset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannel channel;
}
