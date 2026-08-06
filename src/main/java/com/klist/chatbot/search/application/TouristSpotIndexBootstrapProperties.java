package com.klist.chatbot.search.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "search.tourist-spots.bootstrap")
public class TouristSpotIndexBootstrapProperties {

    private TouristSpotIndexBootstrapMode mode = TouristSpotIndexBootstrapMode.NONE;

    public TouristSpotIndexBootstrapMode getMode() {
        return mode;
    }

    public void setMode(TouristSpotIndexBootstrapMode mode) {
        this.mode = mode;
    }

    public void validate() {
        if (mode == null) {
            throw new IllegalStateException("Tourist spot index bootstrap mode is required.");
        }
    }
}
