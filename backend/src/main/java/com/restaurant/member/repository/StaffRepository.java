package com.restaurant.member.repository;

import com.restaurant.member.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByStaffNo(String staffNo);

    List<Staff> findByStatus(Integer status);
}