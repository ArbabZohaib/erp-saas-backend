package com.erp.core.module;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ModulePathMapping {

    private static final Map<String, ModuleCode> PREFIX_TO_MODULE = Map.ofEntries(
            Map.entry("/api/v1/customers", ModuleCode.CUSTOMERS),
            Map.entry("/api/v1/orders", ModuleCode.ORDERS),
            Map.entry("/api/v1/payments", ModuleCode.PAYMENTS),
            Map.entry("/api/v1/expenses", ModuleCode.EXPENSES),
            Map.entry("/api/v1/sales", ModuleCode.SALES),
            Map.entry("/api/v1/hr", ModuleCode.HR),
            Map.entry("/api/v1/reminders", ModuleCode.REMINDERS),
            Map.entry("/api/v1/analytics", ModuleCode.ANALYTICS),
            Map.entry("/api/v1/billing", ModuleCode.BILLING)
    );

    public Optional<ModuleCode> resolveModule(String requestUri) {
        if (requestUri == null) {
            return Optional.empty();
        }

        int q = requestUri.indexOf('?');
        final String path = (q >= 0)
                ? requestUri.substring(0, q)
                : requestUri;

        return PREFIX_TO_MODULE.entrySet().stream()
                .filter(e -> path.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
