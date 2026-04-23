package com.erp.modules.reminders;

import com.erp.core.notifications.NotificationService;
import com.erp.core.tenant.TenantContext;
import com.erp.modules.customers.api.CustomerContactDto;
import com.erp.modules.customers.api.CustomerLookup;
import com.erp.modules.orders.api.OrderLookup;
import com.erp.modules.orders.api.OrderReminderQuery;
import com.erp.modules.orders.api.OrderReminderView;
import com.erp.modules.payments.api.PaymentTotals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ReminderEngineService {

    private final OrderReminderQuery orderReminderQuery;
    private final ReminderRuleRepository reminderRuleRepository;
    private final ReminderRepository reminderRepository;
    private final CustomerLookup customerLookup;
    private final OrderLookup orderLookup;
    private final PaymentTotals paymentTotals;
    private final NotificationService notificationService;

    @Transactional
    public void processDaily(LocalDate today) {
        for (OrderReminderView order : orderReminderQuery.listUnpaidOrdersForReminders(today)) {
            TenantContext.setOrgId(order.orgId());
            try {
                processOrder(order, today);
            } finally {
                TenantContext.clear();
            }
        }
    }

    private void processOrder(OrderReminderView order, LocalDate today) {
        var rules = reminderRuleRepository.findByOrgId(order.orgId());
        Instant scheduledAt = today.atStartOfDay(ZoneOffset.UTC).toInstant();

        for (ReminderRule rule : rules) {
            LocalDate triggerDay = order.dueDate().plusDays(rule.getDaysOffset());
            if (!today.equals(triggerDay)) {
                continue;
            }
            if (reminderRepository.existsByOrgIdAndOrderIdAndReminderRuleIdAndScheduledAt(
                    order.orgId(), order.id(), rule.getId(), scheduledAt)) {
                continue;
            }

            ReminderType reminderType = resolveReminderType(today, order.dueDate());
            Reminder reminder = new Reminder();
            reminder.setOrgId(order.orgId());
            reminder.setCustomerId(order.customerId());
            reminder.setOrderId(order.id());
            reminder.setType(reminderType);
            reminder.setScheduledAt(scheduledAt);
            reminder.setStatus(ReminderStatus.PENDING);
            reminder.setReminderRuleId(rule.getId());
            reminderRepository.save(reminder);

            dispatchNotification(order.orgId(), order.id(), order.customerId(), order.dueDate(), rule, reminderType);
            reminder.setStatus(ReminderStatus.SENT);
            reminderRepository.save(reminder);
        }
    }

    private ReminderType resolveReminderType(LocalDate today, LocalDate dueDate) {
        if (today.isBefore(dueDate)) {
            return ReminderType.BEFORE_DUE;
        }
        if (today.isEqual(dueDate)) {
            return ReminderType.DUE_TODAY;
        }
        return ReminderType.OVERDUE;
    }

    private void dispatchNotification(
            java.util.UUID orgId,
            java.util.UUID orderId,
            java.util.UUID customerId,
            LocalDate dueDate,
            ReminderRule rule,
            ReminderType reminderType
    ) {
        var summary = orderLookup.getSummary(orgId, orderId);
        BigDecimal paid = paymentTotals.getTotalPaidForOrder(orgId, orderId);
        BigDecimal due = summary.totalAmount().subtract(paid);
        CustomerContactDto contact = customerLookup.getContactForNotifications(orgId, customerId);

        String body = "Reminder [%s]: Customer %s — due amount %s — due date %s"
                .formatted(reminderType, contact.name(), due, dueDate);

        switch (rule.getChannel()) {
            case EMAIL -> notificationService.sendEmail(
                    orgId,
                    contact.email() != null ? contact.email() : "",
                    "Payment reminder",
                    body
            );
            case SMS -> notificationService.sendSms(
                    orgId,
                    contact.phone() != null ? contact.phone() : "",
                    body
            );
            case WHATSAPP -> notificationService.sendWhatsApp(
                    orgId,
                    contact.whatsappNumber() != null ? contact.whatsappNumber() : "",
                    body
            );
        }
    }
}
