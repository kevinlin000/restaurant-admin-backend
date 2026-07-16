package com.restaurant.member.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.member.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = { "role" })
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);

    List<User> findByRole_RoleName(String roleName);

    long countByRole_RoleName(String roleName);

    long countByRole_RoleNameAndCreatedAtBetween(String roleName, LocalDateTime start, LocalDateTime end);

    Page<User> findAll(Pageable pageable);

}