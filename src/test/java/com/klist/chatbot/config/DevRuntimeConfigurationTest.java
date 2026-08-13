package com.klist.chatbot.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class DevRuntimeConfigurationTest {

    @Test
    void usesDevRuntimeDefaults() throws IOException {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(
                "devRuntime",
                new ClassPathResource("application-dev.yml")
        );

        assertThat(sources).hasSize(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) sources.get(0).getSource();
        assertThat(properties.get("server.port")).hasToString("${SERVER_PORT:8082}");
        assertThat(properties.get("management.health.elasticsearch.enabled")).hasToString("false");
        assertThat(properties.get("management.health.redis.enabled")).hasToString("false");
    }
}
