package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, Category.CategoryType type);

    List<Category> findByUserIdAndParentIsNull(Long userId);

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.parent.id = :parentId")
    List<Category> findByUserIdAndParentId(Long userId, Long parentId);
}
