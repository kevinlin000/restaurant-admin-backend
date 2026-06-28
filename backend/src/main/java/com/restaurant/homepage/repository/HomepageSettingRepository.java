package com.restaurant.homepage.repository;

import com.restaurant.homepage.entity.HomepageSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomepageSettingRepository extends JpaRepository<HomepageSetting, Long> {
}
