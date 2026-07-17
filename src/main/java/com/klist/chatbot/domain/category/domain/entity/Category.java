package com.klist.chatbot.domain.category.domain.entity;

import com.klist.chatbot.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "category",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_content_type_small_code",
                        columnNames = {"content_type_id", "small_category_code"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "content_type_id", nullable = false)
    private Integer contentTypeId;

    @Column(name = "large_category_code", nullable = false, length = 20)
    private String largeCategoryCode;

    @Column(name = "middle_category_code", nullable = false, length = 20)
    private String middleCategoryCode;

    @Column(name = "small_category_code", nullable = false, length = 20)
    private String smallCategoryCode;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Builder
    private Category(
            Integer contentTypeId,
            String largeCategoryCode,
            String middleCategoryCode,
            String smallCategoryCode,
            String categoryName
    ) {
        this.contentTypeId = contentTypeId;
        this.largeCategoryCode = largeCategoryCode;
        this.middleCategoryCode = middleCategoryCode;
        this.smallCategoryCode = smallCategoryCode;
        this.categoryName = categoryName;
    }
}
