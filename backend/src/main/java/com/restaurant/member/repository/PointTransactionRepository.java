package com.restaurant.member.repository;

import com.restaurant.member.entity.PointTransaction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByReferenceId(Long referenceId);

    boolean existsByReferenceIdAndTransactionType(
            Long referenceId,
            PointTransaction.TransactionType transactionType);

    @EntityGraph(attributePaths = { "store" })
    List<PointTransaction> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<PointTransaction> findByStore_StoreId(Long storeId);

    List<PointTransaction> findByUser_UserIdAndTransactionType(
            Long userId,
            PointTransaction.TransactionType type);
}