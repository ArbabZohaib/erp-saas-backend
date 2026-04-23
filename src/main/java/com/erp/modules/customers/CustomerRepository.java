package com.erp.modules.customers;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByOrgIdOrderByNameAsc(UUID orgId);

    Optional<Customer> findByIdAndOrgId(UUID id, UUID orgId);
}
