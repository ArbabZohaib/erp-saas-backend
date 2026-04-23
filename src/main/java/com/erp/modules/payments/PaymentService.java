package com.erp.modules.payments;

import com.erp.core.tenant.TenantContext;
import com.erp.modules.orders.api.OrderLookup;
import com.erp.modules.orders.api.OrderStatusUpdater;
import com.erp.modules.orders.api.OrderSummary;
import com.erp.modules.payments.api.PaymentTotals;
import com.erp.modules.payments.dto.PaymentRequest;
import com.erp.modules.payments.dto.PaymentResponse;
import com.erp.modules.payments.mapper.PaymentMapper;
import com.erp.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentTotals {

    private final PaymentRepository paymentRepository;
    private final OrderLookup orderLookup;
    private final OrderStatusUpdater orderStatusUpdater;
    private final PaymentMapper paymentMapper;

    @Transactional(readOnly = true)
    public List<PaymentResponse> listByOrder(UUID orderId) {
        UUID orgId = requireOrg();
        orderLookup.getSummary(orgId, orderId);
        return paymentRepository.findByOrgIdAndOrderIdOrderByPaymentDateDesc(orgId, orderId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse record(PaymentRequest request) {
        UUID orgId = requireOrg();
        OrderSummary order = orderLookup.getSummary(orgId, request.orderId());
        if (request.amount().signum() <= 0) {
            throw new BusinessException("Amount must be positive");
        }
        Payment p = new Payment();
        p.setOrgId(orgId);
        p.setOrderId(request.orderId());
        p.setAmount(request.amount());
        p.setPaymentDate(request.paymentDate());
        p.setMethod(request.method());
        paymentRepository.save(p);

        BigDecimal totalPaid = paymentRepository.sumAmountByOrgIdAndOrderId(orgId, request.orderId());
        if (totalPaid.compareTo(order.totalAmount()) > 0) {
            throw new BusinessException("Payments exceed order total");
        }
        orderStatusUpdater.applyPaidAmount(orgId, request.orderId(), totalPaid);
        return paymentMapper.toResponse(p);
    }

    @Transactional(readOnly = true)
    public BigDecimal getOutstandingByCustomer(UUID customerId) {
        UUID orgId = requireOrg();
        BigDecimal ordersTotal = orderLookup.sumTotalAmountForCustomer(orgId, customerId);
        BigDecimal paid = paymentRepository.sumAmountForCustomer(orgId, customerId);
        return ordersTotal.subtract(paid);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getTotalPaidForOrder(UUID orgId, UUID orderId) {
        return paymentRepository.sumAmountByOrgIdAndOrderId(orgId, orderId);
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
