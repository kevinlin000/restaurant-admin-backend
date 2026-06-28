package com.restaurant.faq.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "faq_search_log",
        indexes = {
                @Index(name = "idx_faq_search_log_created_at", columnList = "created_at"),
                @Index(name = "idx_faq_search_log_matched", columnList = "matched"),
                @Index(name = "idx_faq_search_log_normalized_query", columnList = "normalized_query"),
                @Index(name = "idx_faq_search_log_top_faq", columnList = "top_faq_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqSearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "query_text", nullable = false, length = 255)
    private String queryText;

    @Column(name = "normalized_query", nullable = false, length = 255)
    private String normalizedQuery;

    @Column(name = "matched", nullable = false)
    private Boolean matched;

    @Column(name = "result_count", nullable = false)
    private Integer resultCount;

    @Column(name = "top_faq_id")
    private Long topFaqId;

    @Column(name = "top_question", length = 255)
    private String topQuestion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
