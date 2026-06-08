package com.restaurant.member.repository;

import com.restaurant.member.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    Optional<PointTransaction> findByReferenceId(Long referenceId);

    List<PointTransaction> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<PointTransaction> findByStore_StoreId(Long storeId);
}
