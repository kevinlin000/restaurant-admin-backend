package com.restaurant.member.repository;

import com.restaurant.member.entity.MemberProfile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {

    @EntityGraph(attributePaths = { "user" })
    Optional<MemberProfile> findByUserUserId(Long userId);

    List<MemberProfile> findByMemberLevel(MemberProfile.MemberLevel level);
}