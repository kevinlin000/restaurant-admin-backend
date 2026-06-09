package com.restaurant.member.repository;

import com.restaurant.member.entity.PointTransaction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByReferenceId(Long referenceId);

    @EntityGraph(attributePaths = { "store" })
    List<PointTransaction> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = { "user" })
    List<PointTransaction> findByStore_StoreId(Long storeId);

    List<PointTransaction> findByUser_UserIdAndTransactionType(
            Long userId,
            PointTransaction.TransactionType type);
}
