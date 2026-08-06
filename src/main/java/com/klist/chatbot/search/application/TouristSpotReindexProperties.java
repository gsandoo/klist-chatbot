package com.klist.chatbot.search.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "search.tourist-spots.reindex")
public class TouristSpotReindexProperties {

    private int pageSize = 500;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void validate() {
        if (pageSize <= 0) {
            throw new IllegalStateException("Reindex page size must be positive.");
        }
    }
}
