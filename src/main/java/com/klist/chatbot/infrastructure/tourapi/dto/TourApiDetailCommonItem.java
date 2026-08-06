package com.klist.chatbot.infrastructure.tourapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiDetailCommonItem(
        @JsonProperty("contentid") String contentid,
        @JsonProperty("contenttypeid") String contenttypeid,
        @JsonProperty("title") String title,
        @JsonProperty("homepage") String homepage,
        @JsonProperty("overview") String overview,
        @JsonProperty("addr1") String addr1,
        @JsonProperty("addr2") String addr2,
        @JsonProperty("zipcode") String zipcode,
        @JsonProperty("mapx") String mapx,
        @JsonProperty("mapy") String mapy,
        @JsonProperty("tel") String tel,
        @JsonProperty("firstimage") String firstimage,
        @JsonProperty("firstimage2") String firstimage2,
        @JsonProperty("createdtime") String createdtime,
        @JsonProperty("modifiedtime") String modifiedtime
) {
}
