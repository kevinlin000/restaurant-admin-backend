package com.restaurant.member.repository;

import com.restaurant.member.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    Optional<PointTransaction> findByReferenceId(Long referenceId);
}
