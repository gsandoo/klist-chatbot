package com.klist.chatbot.infrastructure.tourapi.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TouristSpotImportData(
        Long tourApiContentId,
        Integer contentTypeId,
        String name,
        String description,
        String address,
        String detailAddress,
        String zipCode,
        BigDecimal latitude,
        BigDecimal longitude,
        String tel,
        String openingHours,
        String admissionFee,
        String officialUrl,
        String reservationUrl,
        String imageUrl,
        String thumbnailImageUrl,
        LocalDateTime sourceCreatedAt,
        LocalDateTime sourceModifiedAt,
        String largeCategoryCode,
        String middleCategoryCode,
        String smallCategoryCode,
        String largeClassificationSystemCode,
        String middleClassificationSystemCode,
        String smallClassificationSystemCode,
        String areaCode,
        String sigunguCode,
        String regionName,
        String legalDongRegionCode,
        String legalDongSigunguCode,
        String language
) {
    public TouristSpotImportData {
        language = language == null ? "ko" : language;
    }

    public TouristSpotImportData(
            Long tourApiContentId,
            Integer contentTypeId,
            String name,
            String description,
            String address,
            String detailAddress,
            String zipCode,
            BigDecimal latitude,
            BigDecimal longitude,
            String tel,
            String openingHours,
            String admissionFee,
            String officialUrl,
            String reservationUrl,
            String imageUrl,
            String thumbnailImageUrl,
            LocalDateTime sourceCreatedAt,
            LocalDateTime sourceModifiedAt,
            String largeCategoryCode,
            String middleCategoryCode,
            String smallCategoryCode,
            String largeClassificationSystemCode,
            String middleClassificationSystemCode,
            String smallClassificationSystemCode,
            String areaCode,
            String sigunguCode,
            String regionName,
            String legalDongRegionCode,
            String legalDongSigunguCode
    ) {
        this(
                tourApiContentId,
                contentTypeId,
                name,
                description,
                address,
                detailAddress,
                zipCode,
                latitude,
                longitude,
                tel,
                openingHours,
                admissionFee,
                officialUrl,
                reservationUrl,
                imageUrl,
                thumbnailImageUrl,
                sourceCreatedAt,
                sourceModifiedAt,
                largeCategoryCode,
                middleCategoryCode,
                smallCategoryCode,
                largeClassificationSystemCode,
                middleClassificationSystemCode,
                smallClassificationSystemCode,
                areaCode,
                sigunguCode,
                regionName,
                legalDongRegionCode,
                legalDongSigunguCode,
                "ko"
        );
    }

}
