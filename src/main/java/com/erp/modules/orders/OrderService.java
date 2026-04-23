package com.erp.modules.orders;

import com.erp.core.tenant.TenantContext;
import com.erp.modules.customers.api.CustomerLookup;
import com.erp.modules.orders.api.OrderLookup;
import com.erp.modules.orders.api.OrderReminderQuery;
import com.erp.modules.orders.api.OrderReminderView;
import com.erp.modules.orders.api.OrderStatusUpdater;
import com.erp.modules.orders.api.OrderSummary;
import com.erp.modules.orders.dto.OrderRequest;
import com.erp.modules.orders.dto.OrderResponse;
import com.erp.modules.orders.mapper.OrderMapper;
import com.erp.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderLookup, OrderStatusUpdater, OrderReminderQuery {

    private final OrderRepository orderRepository;
    private final CustomerLookup customerLookup;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        UUID orgId = requireOrg();
        return orderRepository.findByOrgIdOrderByOrderDateDesc(orgId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listByCustomer(UUID customerId) {
        UUID orgId = requireOrg();
        customerLookup.assertExists(orgId, customerId);
        return orderRepository.findByOrgIdAndCustomerIdOrderByOrderDateDesc(orgId, customerId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return orderMapper.toResponse(getEntity(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        UUID orgId = requireOrg();
        customerLookup.assertExists(orgId, request.customerId());
        int terms = customerLookup.getPaymentTermsDays(orgId, request.customerId());
        Order order = new Order();
        order.setOrgId(orgId);
        order.setCustomerId(request.customerId());
        order.setOrderDate(request.orderDate());
        order.setTotalAmount(request.totalAmount());
        order.setDueDate(request.orderDate().plusDays(terms));
        order.setStatus(OrderStatus.PENDING);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Order getEntity(UUID id) {
        UUID orgId = requireOrg();
        return orderRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new NotFoundException("Order", id));
    }

    @Transactional(readOnly = true)
    @Override
    public OrderSummary getSummary(UUID orgId, UUID orderId) {
        Order o = orderRepository.findByIdAndOrgId(orderId, orgId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        return new OrderSummary(o.getId(), o.getCustomerId(), o.getTotalAmount(), o.getStatus());
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal sumTotalAmountForCustomer(UUID orgId, UUID customerId) {
        return orderRepository.sumTotalAmountForCustomer(orgId, customerId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderReminderView> listUnpaidOrdersForReminders(LocalDate today) {
        List<Order> past = orderRepository.findUnpaidDueOnOrBefore(today);
        List<Order> future = orderRepository.findUnpaidDueAfter(today);
        return Stream.concat(past.stream(), future.stream())
                .map(o -> new OrderReminderView(o.getId(), o.getOrgId(), o.getCustomerId(), o.getDueDate()))
                .toList();
    }

    @Transactional
    @Override
    public void applyPaidAmount(UUID orgId, UUID orderId, BigDecimal totalPaid) {
        Order o = orderRepository.findByIdAndOrgId(orderId, orgId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        BigDecimal paid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
        if (paid.compareTo(o.getTotalAmount()) >= 0) {
            o.setStatus(OrderStatus.PAID);
        } else if (paid.signum() > 0) {
            o.setStatus(OrderStatus.PARTIAL);
        } else {
            o.setStatus(OrderStatus.PENDING);
        }
        orderRepository.save(o);
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }
}
