package com.klist.chatbot.infrastructure.tourapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiAreaBasedListItem(
        @JsonProperty("contentid") String contentid,
        @JsonProperty("contenttypeid") String contenttypeid,
        @JsonProperty("title") String title,
        @JsonProperty("addr1") String addr1,
        @JsonProperty("addr2") String addr2,
        @JsonProperty("zipcode") String zipcode,
        @JsonProperty("areacode") String areacode,
        @JsonProperty("sigungucode") String sigungucode,
        @JsonProperty("cat1") String cat1,
        @JsonProperty("cat2") String cat2,
        @JsonProperty("cat3") String cat3,
        @JsonProperty("lDongRegnCd") String lDongRegnCd,
        @JsonProperty("lDongSignguCd") String lDongSignguCd,
        @JsonProperty("lclsSystm1") String lclsSystm1,
        @JsonProperty("lclsSystm2") String lclsSystm2,
        @JsonProperty("lclsSystm3") String lclsSystm3,
        @JsonProperty("mapx") String mapx,
        @JsonProperty("mapy") String mapy,
        @JsonProperty("tel") String tel,
        @JsonProperty("firstimage") String firstimage,
        @JsonProperty("firstimage2") String firstimage2,
        @JsonProperty("createdtime") String createdtime,
        @JsonProperty("modifiedtime") String modifiedtime
) {
}
