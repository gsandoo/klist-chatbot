package com.klist.chatbot.infrastructure.llm.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klist.chatbot.chat.application.llm.LlmClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenAiLlmProperties.class)
public class OpenAiLlmConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "llm.openai", name = "enabled", havingValue = "true")
    LlmClient openAiLlmClient(OpenAiLlmProperties properties) {
        OpenAiRestClientFactory restClientFactory = new OpenAiRestClientFactory(properties);
        return new OpenAiResponsesClient(
                restClientFactory::create,
                new ObjectMapper(),
                properties
        );
    }
}
