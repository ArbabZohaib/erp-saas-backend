package com.erp.core.notifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class LoggingNotificationService implements NotificationService {

    @Override
    public void sendEmail(UUID orgId, String to, String subject, String body) {
        log.info("[EMAIL] org={} to={} subject={} body={}", orgId, to, subject, body);
    }

    @Override
    public void sendSms(UUID orgId, String phone, String body) {
        log.info("[SMS] org={} phone={} body={}", orgId, phone, body);
    }

    @Override
    public void sendWhatsApp(UUID orgId, String phone, String body) {
        log.info("[WHATSAPP] org={} phone={} body={}", orgId, phone, body);
    }
}
