package com.eventmanagement.repository;

import com.eventmanagement.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Category} entities.
 * <p>
 * Provides CRUD operations and custom queries for category persistence
 * and hierarchical category retrieval.
 *
 * @author Chibuike Okeke
 * @version 1.0
 * @since 1.0
 * @see JpaRepository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its unique label.
     *
     * @param label the category label
     * @return an {@link Optional} containing the category if found, otherwise empty
     */
    Optional<Category> findByLabel(String label);

    /**
     * Retrieves the full subtree of a category using a recursive query.
     * <p>
     * The result includes the specified category and all of its descendants,
     * ordered by parent-child hierarchy.
     *
     * @param id the root category ID
     * @return a list of categories representing the subtree
     */
    @Query(
            value = """
                WITH RECURSIVE sub AS (
                    SELECT id, label, parent_id
                    FROM categories
                    WHERE id = :id

                    UNION ALL

                    SELECT c.id, c.label, c.parent_id
                    FROM categories c
                    INNER JOIN sub s ON c.parent_id = s.id
                )
                SELECT *
                FROM sub
                ORDER BY parent_id NULLS FIRST, id
                """,
            nativeQuery = true
    )
    List<Category> findSubtreeById(@Param("id") Long id);
}
