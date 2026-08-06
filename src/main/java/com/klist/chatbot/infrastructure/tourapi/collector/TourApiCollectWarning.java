package com.klist.chatbot.infrastructure.tourapi.collector;

public record TourApiCollectWarning(
        TourApiCollectWarningCode code,
        TourApiEndpoint endpoint,
        String message
) {
}
