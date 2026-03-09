package com.echapps.ecom.project.user.repository;

import com.echapps.ecom.project.user.model.AppRole;
import com.echapps.ecom.project.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
     Optional<Role> findByRoleName(AppRole appRole);
}
