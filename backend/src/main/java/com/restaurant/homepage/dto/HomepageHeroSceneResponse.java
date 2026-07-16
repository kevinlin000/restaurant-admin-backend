package com.restaurant.homepage.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomepageHeroSceneResponse {
    private String label;
    private String imageKey;
    private String imageUrl;
    private String eyebrow;
    private String title;
    private List<String> lines;
}
