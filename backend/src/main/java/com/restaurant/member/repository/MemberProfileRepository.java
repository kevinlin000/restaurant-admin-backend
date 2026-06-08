package com.restaurant.member.repository;

import com.restaurant.member.entity.MemberProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
    Optional<MemberProfile> findByUser_UserId(Long userId);
}