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
        String legalDongSigunguCode
) {
}
