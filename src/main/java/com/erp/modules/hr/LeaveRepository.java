package com.erp.modules.hr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaveRepository extends JpaRepository<LeaveRequestEntity, UUID> {

    List<LeaveRequestEntity> findByOrgIdOrderByStartDateDesc(UUID orgId);
}
