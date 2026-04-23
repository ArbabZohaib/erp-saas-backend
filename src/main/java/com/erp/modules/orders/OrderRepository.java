package com.erp.modules.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("select coalesce(sum(o.totalAmount), 0) from SalesOrder o where o.orgId = :orgId and o.customerId = :customerId")
    BigDecimal sumTotalAmountForCustomer(@Param("orgId") UUID orgId, @Param("customerId") UUID customerId);

    List<Order> findByOrgIdOrderByOrderDateDesc(UUID orgId);

    List<Order> findByOrgIdAndCustomerIdOrderByOrderDateDesc(UUID orgId, UUID customerId);

    Optional<Order> findByIdAndOrgId(UUID id, UUID orgId);

    @Query("""
            select o from SalesOrder o
            where o.status <> com.erp.modules.orders.OrderStatus.PAID
              and o.dueDate <= :today
            """)
    List<Order> findUnpaidDueOnOrBefore(@Param("today") LocalDate today);

    @Query("""
            select o from SalesOrder o
            where o.status <> com.erp.modules.orders.OrderStatus.PAID
              and o.dueDate > :today
            """)
    List<Order> findUnpaidDueAfter(@Param("today") LocalDate today);

    @Query("""
            select o
            from SalesOrder o, Customer c
            where o.customerId = c.id
              and o.orgId = c.orgId
              and o.orgId = :orgId
              and o.orderDate >= :fromDate and o.orderDate <= :toDate
              and (:customerId is null or o.customerId = :customerId)
              and (:officerId is null or c.assignedSalesOfficerId = :officerId)
            order by o.orderDate
            """)
    List<Order> findForAnalytics(
            @Param("orgId") UUID orgId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("customerId") UUID customerId,
            @Param("officerId") UUID officerId
    );

    @Query("""
            select coalesce(sum(o.totalAmount), 0)
            from SalesOrder o, Customer c, SalesOfficer so
            where o.customerId = c.id
              and o.orgId = c.orgId
              and c.assignedSalesOfficerId = so.id
              and so.orgId = o.orgId
              and so.userId = :employeeUserId
              and o.orgId = :orgId
              and o.orderDate >= :fromDate and o.orderDate <= :toDate
            """)
    BigDecimal sumForSalesOfficerUserInPeriod(
            @Param("orgId") UUID orgId,
            @Param("employeeUserId") UUID employeeUserId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
