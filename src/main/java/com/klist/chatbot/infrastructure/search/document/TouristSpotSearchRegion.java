package com.klist.chatbot.infrastructure.search.document;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public record TouristSpotSearchRegion(
        @Field(type = FieldType.Long)
        Long regionId,

        @Field(type = FieldType.Keyword)
        String areaCode,

        @Field(type = FieldType.Keyword)
        String sigunguCode,

        @Field(type = FieldType.Keyword)
        String legalDongRegionCode,

        @Field(type = FieldType.Keyword)
        String legalDongSigunguCode
) {
}
