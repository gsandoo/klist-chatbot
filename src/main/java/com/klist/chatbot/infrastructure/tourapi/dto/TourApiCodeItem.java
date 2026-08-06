package com.klist.chatbot.infrastructure.tourapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiCodeItem(
        @JsonProperty("code") String code,
        @JsonProperty("name") String name,
        @JsonProperty("rnum") String rnum
) {
}
