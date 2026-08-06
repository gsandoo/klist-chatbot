package com.klist.chatbot.domain.category.repository;

import com.klist.chatbot.domain.category.domain.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByContentTypeIdAndSmallCategoryCode(Integer contentTypeId, String smallCategoryCode);
}
