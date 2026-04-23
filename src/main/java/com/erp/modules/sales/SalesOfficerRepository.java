package com.erp.modules.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesOfficerRepository extends JpaRepository<SalesOfficer, UUID> {

    List<SalesOfficer> findByOrgIdOrderByNameAsc(UUID orgId);

    Optional<SalesOfficer> findByOrgIdAndUserId(UUID orgId, UUID userId);

    /**
     * Sum of order totals for customers assigned to each officer, for orders in [from, to] (inclusive).
     */
    @Query("""
            select so.id, so.name, coalesce(sum(o.totalAmount), 0), count(o.id)
            from SalesOfficer so
            left join Customer c on c.assignedSalesOfficerId = so.id and c.orgId = so.orgId
            left join SalesOrder o on o.customerId = c.id and o.orgId = so.orgId
                and o.orderDate between :from and :to
            where so.orgId = :orgId
            group by so.id, so.name
            order by so.name
            """)
    List<Object[]> aggregateSalesByOfficerInPeriod(
            @Param("orgId") UUID orgId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
