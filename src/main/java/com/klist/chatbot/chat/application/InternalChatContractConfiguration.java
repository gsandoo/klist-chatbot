package com.klist.chatbot.chat.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class InternalChatContractConfiguration {

    @Bean
    @ConditionalOnMissingBean(InternalChatQueryUseCase.class)
    InternalChatQueryUseCase unavailableInternalChatQueryUseCase() {
        return (request, traceId) -> {
            throw new ChatProcessingUnavailableException(
                    "Chat query processing has not been configured."
            );
        };
    }
}
