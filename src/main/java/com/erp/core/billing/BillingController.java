package com.erp.core.billing;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> status() {
        return Map.of("status", "stub", "message", "Integrate Stripe or PSP here");
    }
}
