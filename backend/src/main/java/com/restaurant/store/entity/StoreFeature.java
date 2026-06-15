package com.restaurant.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_feature")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_id")
    private Long featureId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "feature_key", nullable = false, length = 40)
    private String featureKey;

    @Column(name = "feature_label", nullable = false, length = 30)
    private String featureLabel;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
