package com.erp.core.notifications;

import java.util.UUID;

public interface NotificationService {

    void sendEmail(UUID orgId, String to, String subject, String body);

    void sendSms(UUID orgId, String phone, String body);

    void sendWhatsApp(UUID orgId, String phone, String body);
}
