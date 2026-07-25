package com.klist.chatbot.infrastructure.tourapi.collector;

import com.klist.chatbot.infrastructure.tourapi.dto.TourApiAreaBasedListItem;

public record TourApiCollectRequest(
        String contentId,
        TourApiAreaBasedListItem areaBasedListItem
) {

    public TourApiCollectRequest(String contentId) {
        this(contentId, null);
    }

    public TourApiCollectRequest(TourApiAreaBasedListItem areaBasedListItem) {
        this(areaBasedListItem == null ? null : areaBasedListItem.contentid(), areaBasedListItem);
    }
}
