package com.klist.chatbot.infrastructure.llm.openai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenAiLlmProperties.class)
public class OpenAiLlmConfiguration {
}
