package com.erp.modules.expenses.mapper;

import com.erp.modules.expenses.Expense;
import com.erp.modules.expenses.dto.ExpenseRequest;
import com.erp.modules.expenses.dto.ExpenseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "userEmail", expression = "java((String) null)")
    @Mapping(target = "invoiceAttached", expression = "java(Boolean.FALSE)")
    ExpenseResponse toResponse(Expense entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toEntity(ExpenseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Expense entity, ExpenseRequest request);
}
