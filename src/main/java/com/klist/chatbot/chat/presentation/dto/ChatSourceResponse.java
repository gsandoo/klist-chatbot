package com.klist.chatbot.chat.presentation.dto;

public record ChatSourceResponse(
        Long touristSpotId,
        String title,
        ChatSourceType type,
        Double score,
        String referenceUrl
) {
}
