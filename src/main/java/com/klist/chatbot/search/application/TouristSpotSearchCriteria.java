package com.klist.chatbot.search.application;

public record TouristSpotSearchCriteria(
        String keyword,
        Long regionId,
        String areaCode,
        String sigunguCode,
        Integer contentTypeId,
        Long categoryId,
        String largeCategoryCode,
        String middleCategoryCode,
        String smallCategoryCode,
        Double latitude,
        Double longitude,
        Double radiusKm,
        int size,
        Float minimumScore
) {

    private static final int MAX_SIZE = 50;

    public TouristSpotSearchCriteria {
        keyword = trimToNull(keyword);
        areaCode = trimToNull(areaCode);
        sigunguCode = trimToNull(sigunguCode);
        largeCategoryCode = trimToNull(largeCategoryCode);
        middleCategoryCode = trimToNull(middleCategoryCode);
        smallCategoryCode = trimToNull(smallCategoryCode);

        if (regionId != null && regionId <= 0) {
            throw new IllegalArgumentException("regionId must be positive.");
        }
        if (categoryId != null && categoryId <= 0) {
            throw new IllegalArgumentException("categoryId must be positive.");
        }
        if (contentTypeId != null && contentTypeId <= 0) {
            throw new IllegalArgumentException("contentTypeId must be positive.");
        }
        if (size <= 0 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE + ".");
        }
        if (minimumScore != null && minimumScore < 0) {
            throw new IllegalArgumentException("minimumScore must not be negative.");
        }
        validateDistance(latitude, longitude, radiusKm);
        if (!hasCondition(keyword, regionId, areaCode, sigunguCode, contentTypeId, categoryId,
                largeCategoryCode, middleCategoryCode, smallCategoryCode, radiusKm)) {
            throw new IllegalArgumentException("At least one search condition is required.");
        }
    }

    public boolean hasKeyword() {
        return keyword != null;
    }

    public boolean hasDistance() {
        return radiusKm != null;
    }

    private static void validateDistance(Double latitude, Double longitude, Double radiusKm) {
        boolean anyPresent = latitude != null || longitude != null || radiusKm != null;
        boolean allPresent = latitude != null && longitude != null && radiusKm != null;
        if (anyPresent && !allPresent) {
            throw new IllegalArgumentException("latitude, longitude and radiusKm must be provided together.");
        }
        if (!allPresent) {
            return;
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180.");
        }
        if (radiusKm <= 0 || radiusKm > 500) {
            throw new IllegalArgumentException("radiusKm must be between 0 and 500.");
        }
    }

    private static boolean hasCondition(
            String keyword,
            Long regionId,
            String areaCode,
            String sigunguCode,
            Integer contentTypeId,
            Long categoryId,
            String largeCategoryCode,
            String middleCategoryCode,
            String smallCategoryCode,
            Double radiusKm
    ) {
        return keyword != null || regionId != null || areaCode != null || sigunguCode != null
                || contentTypeId != null || categoryId != null || largeCategoryCode != null
                || middleCategoryCode != null || smallCategoryCode != null || radiusKm != null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
