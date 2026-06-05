package com.restaurant.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.restaurant.member.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT COUNT(*) > 0 FROM user WHERE email = :email", nativeQuery = true)
    boolean existsByEmailIncludingDeleted(@Param("email") String email);
}