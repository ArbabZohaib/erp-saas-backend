package com.erp.modules.customers.mapper;

import com.erp.modules.customers.Customer;
import com.erp.modules.customers.dto.CustomerRequest;
import com.erp.modules.customers.dto.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Customer entity, CustomerRequest request);
}
