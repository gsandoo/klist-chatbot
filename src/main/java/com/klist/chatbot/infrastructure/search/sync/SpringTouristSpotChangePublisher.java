package com.klist.chatbot.infrastructure.search.sync;

import com.klist.chatbot.domain.touristspot.service.TouristSpotChangePublisher;
import com.klist.chatbot.domain.touristspot.service.event.TouristSpotChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringTouristSpotChangePublisher implements TouristSpotChangePublisher {

    private final ApplicationEventPublisher eventPublisher;

    public SpringTouristSpotChangePublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishChanged(Long touristSpotId) {
        eventPublisher.publishEvent(new TouristSpotChangedEvent(touristSpotId));
    }
}
