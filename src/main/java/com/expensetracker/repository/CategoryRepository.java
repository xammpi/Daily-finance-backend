package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    /**
     * Override findAll with EntityGraph to prevent N+1 queries in search results
     * Eagerly fetches user relationship in a single query
     */
    @EntityGraph(attributePaths = {"user"})
    @Override
    Page<Category> findAll(Specification<Category> spec, Pageable pageable);

    /**
     * Find category by ID with user eagerly loaded to avoid N+1 queries
     * Used in get-by-id operations where user ownership check is needed
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Category c WHERE c.id = :id")
    Optional<Category> findByIdWithUser(@Param("id") Long id);

    boolean existsByUserIdAndNameIgnoreCase(Long userid, String name);
}
