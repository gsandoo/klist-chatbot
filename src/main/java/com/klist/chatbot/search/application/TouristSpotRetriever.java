package com.klist.chatbot.search.application;

import java.util.Objects;

public class TouristSpotRetriever {

    private final TouristSpotSearchGateway searchGateway;

    public TouristSpotRetriever(TouristSpotSearchGateway searchGateway) {
        this.searchGateway = searchGateway;
    }

    public TouristSpotSearchResult retrieve(TouristSpotSearchCriteria criteria) {
        return searchGateway.search(Objects.requireNonNull(criteria, "criteria must not be null"));
    }
}
