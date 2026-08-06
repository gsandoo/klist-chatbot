package com.klist.chatbot.infrastructure.llm.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klist.chatbot.chat.application.llm.LlmClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenAiLlmProperties.class)
public class OpenAiLlmConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "llm.openai", name = "enabled", havingValue = "true")
    LlmClient openAiLlmClient(OpenAiLlmProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getResponseTimeout());
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl().toString())
                .requestFactory(requestFactory)
                .build();
        return new OpenAiResponsesClient(restClient, new ObjectMapper(), properties);
    }
}
