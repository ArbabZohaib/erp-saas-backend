package com.erp.modules.orders.mapper;

import com.erp.modules.orders.Order;
import com.erp.modules.orders.dto.OrderResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toResponse(Order entity);
}
