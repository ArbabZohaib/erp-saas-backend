package com.erp.modules.payments.mapper;

import com.erp.modules.payments.Payment;
import com.erp.modules.payments.dto.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment entity);
}
