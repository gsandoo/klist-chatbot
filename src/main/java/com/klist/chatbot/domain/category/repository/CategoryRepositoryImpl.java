package com.klist.chatbot.domain.category.repository;

import com.klist.chatbot.domain.category.domain.entity.Category;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<Category> findByContentTypeIdAndSmallCategoryCode(Integer contentTypeId, String smallCategoryCode) {
        return categoryJpaRepository.findByContentTypeIdAndSmallCategoryCode(contentTypeId, smallCategoryCode);
    }

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(category);
    }
}
