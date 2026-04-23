package com.erp.modules.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findByOrgIdAndSalesOfficerIdOrderByCheckInDesc(UUID orgId, UUID salesOfficerId);
}
