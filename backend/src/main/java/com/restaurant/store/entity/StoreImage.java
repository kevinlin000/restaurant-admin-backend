package com.restaurant.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "caption", length = 100)
    private String caption;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
