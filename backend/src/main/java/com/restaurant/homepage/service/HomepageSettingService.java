package com.restaurant.homepage.service;

import com.restaurant.homepage.dto.HomepageSettingRequest;
import com.restaurant.homepage.dto.HomepageSettingResponse;
import org.springframework.security.core.Authentication;

public interface HomepageSettingService {

    HomepageSettingResponse getPublicSetting();

    HomepageSettingResponse getAdminSetting(Authentication authentication);

    HomepageSettingResponse updateSetting(HomepageSettingRequest request, Authentication authentication);
}
