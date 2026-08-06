package com.klist.chatbot.chat.application.answer;

import java.util.List;
import java.util.Set;

public class ChatAnswerGroundingException extends RuntimeException {

    private final List<Long> unsupportedTouristSpotIds;

    public ChatAnswerGroundingException(Set<Long> unsupportedTouristSpotIds) {
        super("Generated answer contains tourist spots outside the search evidence");
        if (unsupportedTouristSpotIds == null || unsupportedTouristSpotIds.isEmpty()) {
            throw new IllegalArgumentException("unsupportedTouristSpotIds must not be empty");
        }
        this.unsupportedTouristSpotIds = List.copyOf(unsupportedTouristSpotIds);
    }

    public List<Long> unsupportedTouristSpotIds() {
        return unsupportedTouristSpotIds;
    }
}
