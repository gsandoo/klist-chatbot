package com.klist.chatbot.search.application;

public record TouristSpotSearchEvidence(
        Long touristSpotId,
        String title,
        String description,
        String address,
        Long regionId,
        Long categoryId,
        Integer contentTypeId,
        Double latitude,
        Double longitude,
        String imageUrl,
        String phoneNumber,
        String openingHours,
        String admissionFee,
        String reservationUrl,
        float score
) {
}
