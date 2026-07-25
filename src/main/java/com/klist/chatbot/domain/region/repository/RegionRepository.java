package com.klist.chatbot.domain.region.repository;

import com.klist.chatbot.domain.region.domain.entity.Region;
import java.util.Optional;

public interface RegionRepository {

    Optional<Region> findByAreaCodeAndSigunguCode(String areaCode, String sigunguCode);

    Region save(Region region);
}
