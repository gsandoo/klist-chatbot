package com.klist.chatbot.domain.touristspot.service;

import com.klist.chatbot.domain.category.domain.entity.Category;
import com.klist.chatbot.domain.category.repository.CategoryRepository;
import com.klist.chatbot.domain.region.domain.entity.Region;
import com.klist.chatbot.domain.region.repository.RegionRepository;
import com.klist.chatbot.domain.touristspot.domain.entity.TouristSpot;
import com.klist.chatbot.domain.touristspot.repository.TouristSpotRepository;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportResult;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportStatus;
import com.klist.chatbot.domain.touristspot.service.result.TouristSpotImportSummary;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingIssue;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingIssueCode;
import com.klist.chatbot.infrastructure.tourapi.mapper.TourApiMappingResult;
import com.klist.chatbot.infrastructure.tourapi.mapper.TouristSpotImportData;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TouristSpotImportService {

    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;
    private final TouristSpotRepository touristSpotRepository;

    @Transactional
    public TouristSpotImportResult importOne(TourApiMappingResult<TouristSpotImportData> mappingResult) {
        if (mappingResult == null || !mappingResult.isSuccess()) {
            return skippedByMappingFailure(mappingResult);
        }

        TouristSpotImportData data = mappingResult.value();
        List<String> warnings = toWarnings(mappingResult.issues());
        TouristSpotImportStatus requiredFieldStatus = validateRequiredFields(data);
        if (requiredFieldStatus != null) {
            return TouristSpotImportResult.of(
                    data == null ? null : data.tourApiContentId(),
                    requiredFieldStatus,
                    "Required import data is missing.",
                    null,
                    warnings
            );
        }

        return touristSpotRepository.findByTourApiContentId(data.tourApiContentId())
                .map(existing -> updateExisting(existing, data, warnings))
                .orElseGet(() -> createNew(data, warnings));
    }

    @Transactional
    public TouristSpotImportSummary importAll(List<TourApiMappingResult<TouristSpotImportData>> mappingResults) {
        List<TouristSpotImportResult> results = new ArrayList<>();
        for (TourApiMappingResult<TouristSpotImportData> mappingResult : mappingResults) {
            results.add(importOne(mappingResult));
        }
        return TouristSpotImportSummary.from(results);
    }

    private TouristSpotImportResult createNew(TouristSpotImportData data, List<String> warnings) {
        CategoryResolution category = resolveCategory(data, warnings);
        RegionResolution region = resolveRegion(data, warnings);
        TouristSpot touristSpot = TouristSpot.builder()
                .tourApiContentId(data.tourApiContentId())
                .contentTypeId(data.contentTypeId())
                .categoryId(category.id())
                .regionId(region.id())
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
                .lastSyncedAt(LocalDateTime.now())
                .build();
        TouristSpot saved = touristSpotRepository.save(touristSpot);
        return TouristSpotImportResult.of(
                data.tourApiContentId(),
                TouristSpotImportStatus.CREATED,
                "Tourist spot was created.",
                saved.getId(),
                warnings
        );
    }

    private TouristSpotImportResult updateExisting(
            TouristSpot existing,
            TouristSpotImportData data,
            List<String> warnings
    ) {
        LocalDateTime existingModifiedAt = existing.getSourceModifiedAt();
        LocalDateTime incomingModifiedAt = data.sourceModifiedAt();
        if (incomingModifiedAt == null && existingModifiedAt != null) {
            return TouristSpotImportResult.of(data.tourApiContentId(),
                    TouristSpotImportStatus.SKIPPED_MISSING_SOURCE_TIMESTAMP,
                    "Incoming sourceModifiedAt is missing.",
                    existing.getId(),
                    warnings);
        }
        if (incomingModifiedAt == null) {
            return TouristSpotImportResult.of(data.tourApiContentId(),
                    TouristSpotImportStatus.SKIPPED_MISSING_SOURCE_TIMESTAMP,
                    "Both existing and incoming sourceModifiedAt are missing.",
                    existing.getId(),
                    warnings);
        }
        if (existingModifiedAt != null) {
            int compareResult = incomingModifiedAt.compareTo(existingModifiedAt);
            if (compareResult == 0) {
                return TouristSpotImportResult.of(data.tourApiContentId(),
                        TouristSpotImportStatus.SKIPPED_NOT_MODIFIED,
                        "Incoming source data is not modified.",
                        existing.getId(),
                        warnings);
            }
            if (compareResult < 0) {
                return TouristSpotImportResult.of(data.tourApiContentId(),
                        TouristSpotImportStatus.SKIPPED_OLDER_SOURCE,
                        "Incoming source data is older than existing data.",
                        existing.getId(),
                        warnings);
            }
        }

        CategoryResolution category = resolveCategory(data, warnings);
        RegionResolution region = resolveRegion(data, warnings);
        existing.updateFromSource(
                data.contentTypeId(),
                category.id(),
                category.resolved(),
                region.id(),
                region.resolved(),
                data.largeCategoryCode(),
                data.middleCategoryCode(),
                data.smallCategoryCode(),
                data.areaCode(),
                data.sigunguCode(),
                data.legalDongRegionCode(),
                data.legalDongSigunguCode(),
                data.name(),
                data.description(),
                data.address(),
                data.detailAddress(),
                data.zipCode(),
                data.latitude(),
                data.longitude(),
                data.tel(),
                data.openingHours(),
                data.admissionFee(),
                data.officialUrl(),
                data.reservationUrl(),
                data.imageUrl(),
                data.thumbnailImageUrl(),
                data.sourceCreatedAt(),
                data.sourceModifiedAt(),
                LocalDateTime.now()
        );
        return TouristSpotImportResult.of(
                data.tourApiContentId(),
                TouristSpotImportStatus.UPDATED,
                "Tourist spot was updated.",
                existing.getId(),
                warnings
        );
    }

    private CategoryResolution resolveCategory(TouristSpotImportData data, List<String> warnings) {
        if (data.contentTypeId() == null || data.smallCategoryCode() == null) {
            warnings.add("Category code is missing. Existing category relation will be kept when updating.");
            return CategoryResolution.unresolved();
        }
        return categoryRepository.findByContentTypeIdAndSmallCategoryCode(data.contentTypeId(), data.smallCategoryCode())
                .map(category -> {
                    category.updateNameIfPresent(null);
                    return CategoryResolution.resolved(category.getId());
                })
                .orElseGet(() -> createCategoryIfPossible(data, warnings));
    }

    private CategoryResolution createCategoryIfPossible(TouristSpotImportData data, List<String> warnings) {
        if (data.largeCategoryCode() == null || data.middleCategoryCode() == null || data.smallCategoryCode() == null) {
            warnings.add("Category was not created because category hierarchy codes are incomplete.");
            return CategoryResolution.unresolved();
        }
        Category category = Category.builder()
                .contentTypeId(data.contentTypeId())
                .largeCategoryCode(data.largeCategoryCode())
                .middleCategoryCode(data.middleCategoryCode())
                .smallCategoryCode(data.smallCategoryCode())
                .categoryName(null)
                .build();
        Category saved = categoryRepository.save(category);
        return CategoryResolution.resolved(saved.getId());
    }

    private RegionResolution resolveRegion(TouristSpotImportData data, List<String> warnings) {
        if (data.areaCode() == null) {
            warnings.add("Region areaCode is missing. Existing region relation will be kept when updating.");
            return RegionResolution.unresolved();
        }
        return regionRepository.findByAreaCodeAndSigunguCode(data.areaCode(), data.sigunguCode())
                .map(region -> RegionResolution.resolved(region.getId()))
                .orElseGet(() -> createRegionIfPossible(data, warnings));
    }

    private RegionResolution createRegionIfPossible(TouristSpotImportData data, List<String> warnings) {
        if (data.regionName() == null) {
            warnings.add("Region was not created because region name is missing.");
            return RegionResolution.unresolved();
        }
        Region region = Region.builder()
                .areaCode(data.areaCode())
                .sigunguCode(data.sigunguCode())
                .regionName(data.regionName())
                .parentRegionCode(data.sigunguCode() == null ? null : data.areaCode())
                .build();
        Region saved = regionRepository.save(region);
        return RegionResolution.resolved(saved.getId());
    }

    private TouristSpotImportStatus validateRequiredFields(TouristSpotImportData data) {
        if (data == null
                || data.tourApiContentId() == null
                || data.contentTypeId() == null
                || data.name() == null
                || data.name().isBlank()) {
            return TouristSpotImportStatus.SKIPPED_MISSING_REQUIRED_FIELD;
        }
        if (data.sourceModifiedAt() == null) {
            return TouristSpotImportStatus.SKIPPED_MISSING_SOURCE_TIMESTAMP;
        }
        return null;
    }

    private TouristSpotImportResult skippedByMappingFailure(TourApiMappingResult<TouristSpotImportData> mappingResult) {
        List<String> warnings = mappingResult == null ? List.of() : toWarnings(mappingResult.issues());
        TouristSpotImportStatus status = TouristSpotImportStatus.SKIPPED_MISSING_REQUIRED_FIELD;
        if (mappingResult != null && mappingResult.issues().stream()
                .anyMatch(issue -> issue.code() == TourApiMappingIssueCode.UNSUPPORTED_CONTENT_TYPE_ID)) {
            status = TouristSpotImportStatus.SKIPPED_UNSUPPORTED_CONTENT_TYPE;
        } else if (mappingResult != null && mappingResult.issues().stream()
                .anyMatch(issue -> issue.code() == TourApiMappingIssueCode.MISSING_REQUIRED_SOURCE_MODIFIED_AT
                        || "modifiedtime".equals(issue.fieldName()))) {
            status = TouristSpotImportStatus.SKIPPED_MISSING_SOURCE_TIMESTAMP;
        }
        return TouristSpotImportResult.of(null, status, "Mapping failed.", null, warnings);
    }

    private List<String> toWarnings(List<TourApiMappingIssue> issues) {
        List<String> warnings = new ArrayList<>();
        for (TourApiMappingIssue issue : issues) {
            warnings.add(issue.code() + ":" + issue.fieldName() + ":" + issue.message());
        }
        return warnings;
    }

    private record CategoryResolution(Long id, boolean resolved) {

        static CategoryResolution resolved(Long id) {
            return new CategoryResolution(id, true);
        }

        static CategoryResolution unresolved() {
            return new CategoryResolution(null, false);
        }
    }

    private record RegionResolution(Long id, boolean resolved) {

        static RegionResolution resolved(Long id) {
            return new RegionResolution(id, true);
        }

        static RegionResolution unresolved() {
            return new RegionResolution(null, false);
        }
    }
}
