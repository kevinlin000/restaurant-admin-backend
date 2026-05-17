package com.restaurant.repository;

import com.restaurant.entity.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
    // 先放空，基礎的 CRUD（已經內建在 JpaRepository 裡面了
}