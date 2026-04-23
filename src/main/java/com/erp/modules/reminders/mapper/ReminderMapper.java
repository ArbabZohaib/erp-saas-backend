package com.erp.modules.reminders.mapper;

import com.erp.modules.reminders.Reminder;
import com.erp.modules.reminders.ReminderRule;
import com.erp.modules.reminders.dto.ReminderResponse;
import com.erp.modules.reminders.dto.ReminderRuleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReminderMapper {

    ReminderRuleResponse toRuleResponse(ReminderRule entity);

    ReminderResponse toReminderResponse(Reminder entity);
}
