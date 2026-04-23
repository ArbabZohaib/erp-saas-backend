package com.erp.modules.sales.mapper;

import com.erp.modules.sales.SalesOfficer;
import com.erp.modules.sales.dto.SalesOfficerRequest;
import com.erp.modules.sales.dto.SalesOfficerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SalesMapper {

    SalesOfficerResponse toResponse(SalesOfficer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SalesOfficer toEntity(SalesOfficerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget SalesOfficer entity, SalesOfficerRequest request);
}
