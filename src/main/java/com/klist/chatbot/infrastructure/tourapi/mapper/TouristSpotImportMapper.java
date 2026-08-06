package com.klist.chatbot.infrastructure.tourapi.mapper;

import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TouristSpotImportMapper {

    public TourApiMappingResult<TouristSpot> toEntity(TouristSpotImportData data, LocalDateTime lastSyncedAt) {
        List<TourApiMappingIssue> issues = new ArrayList<>();
        if (data == null) {
            issues.add(new TourApiMappingIssue(TourApiMappingIssueCode.MISSING_REQUIRED_CONTENT_ID,
                    "tourApiContentId", "Import data is missing."));
            return TourApiMappingResult.failure(issues);
        }
        if (data.tourApiContentId() == null) {
            issues.add(new TourApiMappingIssue(TourApiMappingIssueCode.MISSING_REQUIRED_CONTENT_ID,
                    "tourApiContentId", "Required normalized field is missing."));
        }
        if (data.contentTypeId() == null) {
            issues.add(new TourApiMappingIssue(TourApiMappingIssueCode.MISSING_REQUIRED_CONTENT_TYPE_ID,
                    "contentTypeId", "Required normalized field is missing."));
        }
        if (data.name() == null || data.name().isBlank()) {
            issues.add(new TourApiMappingIssue(TourApiMappingIssueCode.MISSING_REQUIRED_NAME,
                    "name", "Required normalized field is missing."));
        }
        if (data.sourceModifiedAt() == null) {
            issues.add(new TourApiMappingIssue(TourApiMappingIssueCode.MISSING_REQUIRED_SOURCE_MODIFIED_AT,
                    "sourceModifiedAt", "Required normalized field is missing."));
        }
        if (lastSyncedAt == null) {
            issues.add(new TourApiMappingIssue(TourApiMappingIssueCode.MISSING_REQUIRED_SOURCE_MODIFIED_AT,
                    "lastSyncedAt", "Internal sync time is required."));
        }
        if (!issues.isEmpty()) {
            return TourApiMappingResult.failure(issues);
        }

        TouristSpot touristSpot = TouristSpot.builder()
                .tourApiContentId(data.tourApiContentId())
                .contentTypeId(data.contentTypeId())
                .categoryId(null)
                .regionId(null)
                .largeCategoryCode(data.largeCategoryCode())
                .middleCategoryCode(data.middleCategoryCode())
                .smallCategoryCode(data.smallCategoryCode())
                .areaCode(data.areaCode())
                .sigunguCode(data.sigunguCode())
                .legalDongRegionCode(data.legalDongRegionCode())
                .legalDongSigunguCode(data.legalDongSigunguCode())
                .name(data.name())
                .description(data.description())
                .address(data.address())
                .detailAddress(data.detailAddress())
                .zipCode(data.zipCode())
                .latitude(data.latitude())
                .longitude(data.longitude())
                .tel(data.tel())
                .openingHours(data.openingHours())
                .admissionFee(data.admissionFee())
                .officialUrl(data.officialUrl())
                .reservationUrl(data.reservationUrl())
                .imageUrl(data.imageUrl())
                .thumbnailImageUrl(data.thumbnailImageUrl())
                .sourceCreatedAt(data.sourceCreatedAt())
                .sourceModifiedAt(data.sourceModifiedAt())
                .lastSyncedAt(lastSyncedAt)
                .build();
        return TourApiMappingResult.success(touristSpot, issues);
    }
}
