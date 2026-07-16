package com.restaurant.faq.repository;

import com.restaurant.faq.entity.FaqSearchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FaqSearchLogRepository extends JpaRepository<FaqSearchLog, Long> {

    List<FaqSearchLog> findTop50ByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime since);

    long countByMatchedFalseAndCreatedAtAfter(LocalDateTime since);

    @Query("""
            select count(distinct log.normalizedQuery)
            from FaqSearchLog log
            where log.createdAt >= :since
            """)
    long countDistinctQueriesSince(@Param("since") LocalDateTime since);

    @Query("""
            select log.normalizedQuery, count(log), max(log.createdAt)
            from FaqSearchLog log
            where log.createdAt >= :since
            group by log.normalizedQuery
            order by count(log) desc, max(log.createdAt) desc
            """)
    List<Object[]> findPopularQueries(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("""
            select log.normalizedQuery, count(log), max(log.createdAt)
            from FaqSearchLog log
            where log.createdAt >= :since and log.matched = false
            group by log.normalizedQuery
            order by count(log) desc, max(log.createdAt) desc
            """)
    List<Object[]> findMissedQueries(@Param("since") LocalDateTime since, Pageable pageable);
}
