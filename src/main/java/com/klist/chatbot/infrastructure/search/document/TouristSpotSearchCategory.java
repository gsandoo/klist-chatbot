package com.klist.chatbot.infrastructure.search.document;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public record TouristSpotSearchCategory(
        @Field(type = FieldType.Long)
        Long categoryId,

        @Field(type = FieldType.Integer)
        Integer contentTypeId,

        @Field(type = FieldType.Keyword)
        String largeCategoryCode,

        @Field(type = FieldType.Keyword)
        String middleCategoryCode,

        @Field(type = FieldType.Keyword)
        String smallCategoryCode
) {
}
