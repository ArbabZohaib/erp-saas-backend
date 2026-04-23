package com.erp.modules.hr.mapper;

import com.erp.modules.hr.LeaveRequestEntity;
import com.erp.modules.hr.Salary;
import com.erp.modules.hr.dto.LeaveRequestDto;
import com.erp.modules.hr.dto.LeaveResponse;
import com.erp.modules.hr.dto.SalaryRequest;
import com.erp.modules.hr.dto.SalaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HrMapper {

    LeaveResponse toLeaveResponse(LeaveRequestEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    LeaveRequestEntity toLeaveEntity(LeaveRequestDto dto);

    SalaryResponse toSalaryResponse(Salary entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Salary toSalaryEntity(SalaryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateSalary(@MappingTarget Salary entity, SalaryRequest request);
}
