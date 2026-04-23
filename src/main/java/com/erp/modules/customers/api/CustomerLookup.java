package com.erp.modules.customers.api;

import java.util.UUID;

/**
 * Cross-module facade: other modules must use this instead of {@code CustomerRepository}.
 */
public interface CustomerLookup {

    CustomerContactDto getContactForNotifications(UUID orgId, UUID customerId);

    void assertExists(UUID orgId, UUID customerId);

    /**
     * Data required to create orders without exposing persistence details.
     */
    int getPaymentTermsDays(UUID orgId, UUID customerId);
}
