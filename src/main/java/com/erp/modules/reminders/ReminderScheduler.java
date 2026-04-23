package com.erp.modules.reminders;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderEngineService reminderEngineService;

    @Scheduled(cron = "${app.reminders.cron:0 0 2 * * *}", zone = "UTC")
    public void runReminders() {
        reminderEngineService.processDaily(LocalDate.now(ZoneOffset.UTC));
    }
}
