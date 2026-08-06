package com.klist.chatbot.infrastructure.search.sync;

public record TouristSpotIndexSyncResult(
        Long touristSpotId,
        TouristSpotIndexSyncStatus status,
        String failureMessage
) {

    public static TouristSpotIndexSyncResult indexed(Long touristSpotId) {
        return new TouristSpotIndexSyncResult(touristSpotId, TouristSpotIndexSyncStatus.INDEXED, null);
    }

    public static TouristSpotIndexSyncResult notFound(Long touristSpotId) {
        return new TouristSpotIndexSyncResult(touristSpotId, TouristSpotIndexSyncStatus.NOT_FOUND, null);
    }

    public static TouristSpotIndexSyncResult failed(Long touristSpotId, RuntimeException exception) {
        return new TouristSpotIndexSyncResult(touristSpotId, TouristSpotIndexSyncStatus.FAILED,
                exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }
}
