package com.klist.chatbot.domain.category.repository;

import com.klist.chatbot.domain.category.domain.entity.Category;
import java.util.Optional;

public interface CategoryRepository {

    Optional<Category> findByContentTypeIdAndSmallCategoryCode(Integer contentTypeId, String smallCategoryCode);

    Category save(Category category);
}
