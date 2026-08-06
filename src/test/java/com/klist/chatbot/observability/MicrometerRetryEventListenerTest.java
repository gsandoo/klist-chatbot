package com.klist.chatbot.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class MicrometerRetryEventListenerTest {

    @Test
    void recordsRetryAndExhaustionCountersWithBoundedTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerRetryEventListener listener = new MicrometerRetryEventListener(registry);

        listener.retrying("openai", "timeout", 2, Duration.ofMillis(100));
        listener.retrying("openai", "timeout", 3, Duration.ofMillis(200));
        listener.exhausted("elasticsearch", "transient", 3);

        assertThat(registry.get("chatbot.retry.attempts")
                .tags("component", "openai", "reason", "timeout")
                .counter().count()).isEqualTo(2.0);
        assertThat(registry.get("chatbot.retry.exhausted")
                .tags("component", "elasticsearch", "reason", "transient")
                .counter().count()).isEqualTo(1.0);
    }
}
