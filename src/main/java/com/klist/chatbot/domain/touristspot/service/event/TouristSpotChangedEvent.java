package com.klist.chatbot.domain.touristspot.service.event;

public record TouristSpotChangedEvent(Long touristSpotId) {

    public TouristSpotChangedEvent {
        if (touristSpotId == null || touristSpotId <= 0) {
            throw new IllegalArgumentException("touristSpotId must be positive.");
        }
    }
}
