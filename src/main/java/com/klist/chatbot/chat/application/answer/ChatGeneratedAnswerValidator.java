package com.klist.chatbot.chat.application.answer;

import com.klist.chatbot.chat.application.evidence.ChatEvidenceContext;
import com.klist.chatbot.chat.application.evidence.ChatTouristSpotEvidence;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class ChatGeneratedAnswerValidator {

    public ChatGeneratedAnswer validate(
            ChatGeneratedAnswer generatedAnswer,
            ChatEvidenceContext evidenceContext
    ) {
        Objects.requireNonNull(generatedAnswer, "generatedAnswer must not be null");
        Objects.requireNonNull(evidenceContext, "evidenceContext must not be null");

        Set<Long> evidenceIds = evidenceContext.touristSpots().stream()
                .map(ChatTouristSpotEvidence::touristSpotId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<Long> unsupportedIds = new LinkedHashSet<>();
        for (ChatRecommendation recommendation : generatedAnswer.recommendations()) {
            if (!evidenceIds.contains(recommendation.touristSpotId())) {
                unsupportedIds.add(recommendation.touristSpotId());
            }
        }
        if (!unsupportedIds.isEmpty()) {
            throw new ChatAnswerGroundingException(unsupportedIds);
        }
        return generatedAnswer;
    }
}
