package com.erp.modules.orders.api;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only facade for the reminder engine (no direct repository access from reminders module).
 */
public interface OrderReminderQuery {

    List<OrderReminderView> listUnpaidOrdersForReminders(LocalDate today);
}
