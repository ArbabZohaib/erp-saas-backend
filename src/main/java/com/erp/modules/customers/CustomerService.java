package com.erp.modules.customers;

import com.erp.core.tenant.TenantContext;
import com.erp.modules.customers.api.CustomerContactDto;
import com.erp.modules.customers.api.CustomerLookup;
import com.erp.modules.customers.dto.CustomerRequest;
import com.erp.modules.customers.dto.CustomerResponse;
import com.erp.modules.customers.mapper.CustomerMapper;
import com.erp.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService implements CustomerLookup {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<CustomerResponse> list() {
        UUID orgId = requireOrg();
        return customerRepository.findByOrgIdOrderByNameAsc(orgId).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        Customer c = getEntity(id);
        return customerMapper.toResponse(c);
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        UUID orgId = requireOrg();
        Customer c = customerMapper.toEntity(request);
        c.setOrgId(orgId);
        return customerMapper.toResponse(customerRepository.save(c));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer c = getEntity(id);
        customerMapper.update(c, request);
        return customerMapper.toResponse(customerRepository.save(c));
    }

    @Transactional(readOnly = true)
    @Override
    public CustomerContactDto getContactForNotifications(UUID orgId, UUID customerId) {
        Customer c = customerRepository.findByIdAndOrgId(customerId, orgId)
                .orElseThrow(() -> new NotFoundException("Customer", customerId));
        return new CustomerContactDto(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getWhatsappNumber());
    }

    @Transactional(readOnly = true)
    @Override
    public void assertExists(UUID orgId, UUID customerId) {
        customerRepository.findByIdAndOrgId(customerId, orgId)
                .orElseThrow(() -> new NotFoundException("Customer", customerId));
    }

    @Transactional(readOnly = true)
    @Override
    public int getPaymentTermsDays(UUID orgId, UUID customerId) {
        Customer c = customerRepository.findByIdAndOrgId(customerId, orgId)
                .orElseThrow(() -> new NotFoundException("Customer", customerId));
        return c.getPaymentTermsDays();
    }

    Customer getEntity(UUID id) {
        UUID orgId = requireOrg();
        return customerRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new NotFoundException("Customer", id));
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
