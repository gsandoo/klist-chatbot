package com.klist.chatbot.domain.category.service;

import com.klist.chatbot.domain.category.domain.entity.Category;
import com.klist.chatbot.domain.category.repository.CategoryRepository;
import com.klist.chatbot.domain.category.service.result.CategoryResolution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryPersistenceResolver {

    private final CategoryRepository categoryRepository;

    public CategoryResolution resolve(
            Integer contentTypeId,
            String largeCategoryCode,
            String middleCategoryCode,
            String smallCategoryCode,
            String categoryName
    ) {
        if (contentTypeId == null || smallCategoryCode == null) {
            return CategoryResolution.unresolved(
                    "Category code is missing. Existing category relation will be kept when updating."
            );
        }

        return categoryRepository.findByContentTypeIdAndSmallCategoryCode(contentTypeId, smallCategoryCode)
                .map(category -> {
                    category.updateNameIfPresent(categoryName);
                    return CategoryResolution.resolved(category.getId());
                })
                .orElseGet(() -> createIfPossible(
                        contentTypeId,
                        largeCategoryCode,
                        middleCategoryCode,
                        smallCategoryCode,
                        categoryName
                ));
    }

    private CategoryResolution createIfPossible(
            Integer contentTypeId,
            String largeCategoryCode,
            String middleCategoryCode,
            String smallCategoryCode,
            String categoryName
    ) {
        if (largeCategoryCode == null || middleCategoryCode == null || smallCategoryCode == null) {
            return CategoryResolution.unresolved(
                    "Category was not created because category hierarchy codes are incomplete."
            );
        }

        Category category = Category.builder()
                .contentTypeId(contentTypeId)
                .largeCategoryCode(largeCategoryCode)
                .middleCategoryCode(middleCategoryCode)
                .smallCategoryCode(smallCategoryCode)
                .categoryName(categoryName)
                .build();
        return CategoryResolution.resolved(categoryRepository.save(category).getId());
    }
}
