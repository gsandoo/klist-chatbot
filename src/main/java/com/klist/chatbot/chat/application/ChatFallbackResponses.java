package com.klist.chatbot.chat.application;

import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalysis;
import java.util.List;

final class ChatFallbackResponses {

    private ChatFallbackResponses() {
    }

    static ChatFallbackResponse noResult(ChatQuestionAnalysis analysis, String language) {
        return "en".equals(language) ? new ChatFallbackResponse(
                "I could not find matching tourism information. Try a broader location or another attraction type.",
                List.of("Recommend places to visit in Seoul", "Find restaurants in Busan")) : noResult(analysis);
    }

    static ChatFallbackResponse unsupported(String language) {
        return "en".equals(language) ? new ChatFallbackResponse(
                "I can help with tourism information and travel recommendations in Korea.",
                List.of("Recommend places to visit in Seoul")) : unsupported();
    }

    static ChatFallbackResponse clarificationRequired(String language) {
        return "en".equals(language) ? new ChatFallbackResponse(
                "Which location and type of attraction would you like to explore?",
                List.of("Recommend museums in Seoul")) : clarificationRequired();
    }

    static ChatFallbackResponse noResult(ChatQuestionAnalysis analysis) {
        return new ChatFallbackResponse(
                ChatNoResultGuidance.message(analysis),
                List.of(
                        "지역 범위를 넓혀서 관광지를 추천해 주세요",
                        "다른 관광 유형으로 추천해 주세요"
                )
        );
    }

    static ChatFallbackResponse unsupported() {
        return new ChatFallbackResponse(
                "관광지 검색과 여행 추천에 관한 질문에 답변할 수 있습니다. 지역이나 원하는 관광 유형을 포함해 질문해 주세요.",
                List.of(
                        "서울에서 방문할 만한 관광지를 추천해 주세요",
                        "부산 맛집을 추천해 주세요"
                )
        );
    }

    static ChatFallbackResponse clarificationRequired() {
        return new ChatFallbackResponse(
                "어느 지역에서 어떤 관광지를 찾으시는지 조금 더 알려주세요.",
                List.of(
                        "서울에서 아이와 갈 만한 곳을 추천해 주세요",
                        "제주도의 자연 관광지를 추천해 주세요"
                )
        );
    }
}
