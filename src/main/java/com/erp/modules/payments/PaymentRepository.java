package com.erp.modules.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.orgId = :orgId and p.orderId = :orderId")
    BigDecimal sumAmountByOrgIdAndOrderId(@Param("orgId") UUID orgId, @Param("orderId") UUID orderId);

    @Query("""
            select coalesce(sum(p.amount), 0) from Payment p
            join SalesOrder o on p.orderId = o.id and p.orgId = o.orgId
            where o.orgId = :orgId and o.customerId = :customerId
            """)
    BigDecimal sumAmountForCustomer(@Param("orgId") UUID orgId, @Param("customerId") UUID customerId);

    List<Payment> findByOrgIdAndOrderIdOrderByPaymentDateDesc(UUID orgId, UUID orderId);
}
