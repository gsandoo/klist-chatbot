package com.klist.chatbot.chat.application;

import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalyzer;
import com.klist.chatbot.chat.application.evidence.ChatSearchEvidenceOrganizer;
import com.klist.chatbot.search.application.TouristSpotRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ChatOrchestrationConfiguration {

    @Bean
    ChatSearchEvidenceOrganizer chatSearchEvidenceOrganizer() {
        return new ChatSearchEvidenceOrganizer();
    }

    @Bean
    ChatSearchOrchestrator chatSearchOrchestrator(
            ChatQuestionAnalyzer questionAnalyzer,
            TouristSpotRetriever touristSpotRetriever,
            ChatSearchEvidenceOrganizer evidenceOrganizer
    ) {
        return new ChatSearchOrchestrator(questionAnalyzer, touristSpotRetriever, evidenceOrganizer);
    }
}
