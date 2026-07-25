package com.klist.chatbot.domain.region.service;

import com.klist.chatbot.domain.region.domain.entity.Region;
import com.klist.chatbot.domain.region.repository.RegionRepository;
import com.klist.chatbot.domain.region.service.result.RegionResolution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegionPersistenceResolver {

    private final RegionRepository regionRepository;

    public RegionResolution resolve(String areaCode, String sigunguCode, String regionName) {
        if (areaCode == null) {
            return RegionResolution.unresolved(
                    "Region areaCode is missing. Existing region relation will be kept when updating."
            );
        }

        return regionRepository.findByAreaCodeAndSigunguCode(areaCode, sigunguCode)
                .map(region -> RegionResolution.resolved(region.getId()))
                .orElseGet(() -> createIfPossible(areaCode, sigunguCode, regionName));
    }

    private RegionResolution createIfPossible(String areaCode, String sigunguCode, String regionName) {
        if (regionName == null) {
            return RegionResolution.unresolved("Region was not created because region name is missing.");
        }

        Region region = Region.builder()
                .areaCode(areaCode)
                .sigunguCode(sigunguCode)
                .regionName(regionName)
                .parentRegionCode(sigunguCode == null ? null : areaCode)
                .build();
        return RegionResolution.resolved(regionRepository.save(region).getId());
    }
}
