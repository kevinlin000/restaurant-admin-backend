package com.restaurant.homepage.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "homepage_setting")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomepageSetting {

    @Id
    @Column(name = "setting_id")
    private Long settingId;

    @Lob
    @Column(name = "hero_scenes_json", columnDefinition = "TEXT")
    private String heroScenesJson;

    @Column(name = "story_kicker", nullable = false, length = 80)
    private String storyKicker;

    @Column(name = "story_title", nullable = false, length = 160)
    private String storyTitle;

    @Column(name = "story_description", nullable = false, length = 700)
    private String storyDescription;

    @Column(name = "signature_kicker", nullable = false, length = 80)
    private String signatureKicker;

    @Column(name = "signature_title", nullable = false, length = 160)
    private String signatureTitle;

    @Column(name = "store_kicker", nullable = false, length = 80)
    private String storeKicker;

    @Column(name = "store_title", nullable = false, length = 160)
    private String storeTitle;

    @Column(name = "store_description", nullable = false, length = 360)
    private String storeDescription;

    @Column(name = "news_kicker", nullable = false, length = 80)
    private String newsKicker;

    @Column(name = "news_title", nullable = false, length = 160)
    private String newsTitle;

    @Column(name = "reservation_kicker", nullable = false, length = 80)
    private String reservationKicker;

    @Column(name = "reservation_title", nullable = false, length = 160)
    private String reservationTitle;

    @Column(name = "reservation_description", nullable = false, length = 700)
    private String reservationDescription;

    @Column(name = "featured_store_ids", length = 240)
    private String featuredStoreIds;

    @Column(name = "featured_news_ids", length = 240)
    private String featuredNewsIds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
