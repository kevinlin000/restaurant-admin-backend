package com.restaurant.repository;

import com.restaurant.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // 只要宣告這個名字，JPA 就會自動幫你寫好：SELECT * FROM role WHERE role_name = ?
    Optional<Role> findByRoleName(String roleName);
}