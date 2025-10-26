package com.ooadproject.backend.repositories;

import com.ooadproject.backend.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface
CategoryRepository extends JpaRepository<Category, Integer> {

    // Find by name (case-insensitive)
    Optional<Category> findByNameIgnoreCase(String name);

    // Check if name exists (for validation)
    boolean existsByNameIgnoreCase(String name);

    // Find categories with products
    @Query("SELECT c FROM Category c WHERE EXISTS (SELECT p FROM Product p WHERE p.category = c)")
    List<Category> findCategoriesWithProducts();

    // Search categories by name
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> searchByNameIgnoreCase(@Param("name") String name);

    // Order by name
    List<Category> findAllByOrderByNameAsc();
}