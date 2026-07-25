package com.klist.chatbot.infrastructure.tourapi.client;

import java.util.List;

public record TourApiPage<T>(
        List<T> items,
        int pageNo,
        int numOfRows,
        int totalCount
) {

    public TourApiPage {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public boolean hasNext() {
        return (long) pageNo * numOfRows < totalCount;
    }
}
