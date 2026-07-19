package com.klist.chatbot.infrastructure.tourapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiDetailImageItem(
        @JsonProperty("contentid") String contentid,
        @JsonProperty("imgname") String imgname,
        @JsonProperty("originimgurl") String originimgurl,
        @JsonProperty("smallimageurl") String smallimageurl,
        @JsonProperty("serialnum") String serialnum,
        @JsonProperty("cpyrhtDivCd") String cpyrhtDivCd
) {
}
