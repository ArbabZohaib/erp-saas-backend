package com.erp.core.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByOrgIdAndEmail(UUID orgId, String email);

    List<AppUser> findByOrgIdOrderByEmailAsc(UUID orgId);
}
