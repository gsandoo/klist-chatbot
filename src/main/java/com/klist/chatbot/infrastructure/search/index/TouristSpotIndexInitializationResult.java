package com.klist.chatbot.infrastructure.search.index;

public record TouristSpotIndexInitializationResult(
        String indexName,
        String alias,
        boolean indexCreated,
        boolean aliasUpdated
) {
}
