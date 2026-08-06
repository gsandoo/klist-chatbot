package com.klist.chatbot.chat.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klist.chatbot.chat.application.analysis.ChatQuestionAnalyzer;
import com.klist.chatbot.chat.application.evidence.ChatSearchEvidenceOrganizer;
import com.klist.chatbot.chat.application.prompt.ChatPromptFactory;
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
    ChatPromptFactory chatPromptFactory() {
        return new ChatPromptFactory(new ObjectMapper());
    }

    @Bean
    ChatSearchOrchestrator chatSearchOrchestrator(
            ChatQuestionAnalyzer questionAnalyzer,
            TouristSpotRetriever touristSpotRetriever,
            ChatSearchEvidenceOrganizer evidenceOrganizer,
            ChatPromptFactory promptFactory
    ) {
        return new ChatSearchOrchestrator(
                questionAnalyzer,
                touristSpotRetriever,
                evidenceOrganizer,
                promptFactory
        );
    }
}
