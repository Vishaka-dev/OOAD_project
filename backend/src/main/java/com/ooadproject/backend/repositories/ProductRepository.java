package com.ooadproject.backend.repositories;

import com.ooadproject.backend.entities.Category;
import com.ooadproject.backend.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Find by category
    List<Product> findByCategoryOrderByNameAsc(Category category);
    List<Product> findByCategoryCategoryIdOrderByNameAsc(Integer categoryId);

    // Find by name (search)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY p.name ASC")
    List<Product> searchByNameIgnoreCase(@Param("name") String name);

    // Find by price range
    List<Product> findByPriceBetweenOrderByPriceAsc(BigDecimal minPrice, BigDecimal maxPrice);

    // Find in stock products
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 ORDER BY p.name ASC")
    List<Product> findInStockProducts();

    // Basic search with filters
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.categoryId = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:inStock IS NULL OR (:inStock = true AND p.stockQuantity > 0) OR (:inStock = false))")
    Page<Product> searchProducts(@Param("name") String name,
                                 @Param("categoryId") Integer categoryId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("inStock") Boolean inStock,
                                 Pageable pageable);

    // Stock management
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // Order by name
    List<Product> findAllByOrderByNameAsc();
}