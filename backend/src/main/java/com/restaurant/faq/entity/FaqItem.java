package com.restaurant.faq.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "faq_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_faq_question", columnNames = "question"),
        indexes = {
                @Index(name = "idx_faq_public", columnList = "is_deleted,status,category,is_featured,sort_order"),
                @Index(name = "idx_faq_admin", columnList = "is_deleted,is_featured,sort_order")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faq_id")
    private Long faqId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private FaqCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FaqStatus status = FaqStatus.DRAFT;

    @Column(name = "question", nullable = false, length = 180)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "keywords", length = 320)
    private String keywords;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
