package com.restaurant.member.repository;

import com.restaurant.member.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByStaffNo(String staffNo);

    Optional<Staff> findByUser_UserId(Long userId);

    @EntityGraph(attributePaths = { "store", "user" })
    Optional<Staff> findByUser_UserIdAndStatus(Long userId, Staff.StaffStatus status);

    List<Staff> findByStatus(Staff.StaffStatus status);

    List<Staff> findByStore_StoreIdAndStatus(Long storeId, Staff.StaffStatus status);
}
