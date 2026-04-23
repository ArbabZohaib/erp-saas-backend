package com.erp.modules.reminders;

import com.erp.modules.reminders.dto.ReminderResponse;
import com.erp.modules.reminders.dto.ReminderRuleRequest;
import com.erp.modules.reminders.dto.ReminderRuleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderFacadeService reminderFacadeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ReminderResponse> listReminders() {
        return reminderFacadeService.listReminders();
    }

    @GetMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ReminderRuleResponse> listRules() {
        return reminderFacadeService.listRules();
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ReminderRuleResponse createRule(@Valid @RequestBody ReminderRuleRequest request) {
        return reminderFacadeService.createRule(request);
    }

    @PutMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReminderRuleResponse updateRule(@PathVariable UUID id, @Valid @RequestBody ReminderRuleRequest request) {
        return reminderFacadeService.updateRule(id, request);
    }
}
