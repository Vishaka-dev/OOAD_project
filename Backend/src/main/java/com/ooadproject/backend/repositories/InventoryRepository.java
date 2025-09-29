package com.ooadproject.backend.repositories;

import com.ooadproject.backend.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    /**
     * Find inventory record by the associated product's ID.
     */
    Optional<Inventory> findByProductProductId(Integer productId);

    /**
     * Get all inventory items that are considered low stock.
     * (Stock level <= low stock threshold AND stock > 0)
     */
    @Query("SELECT i FROM Inventory i WHERE i.stockLevel <= i.lowStockThreshold AND i.stockLevel > 0")
    List<Inventory> findLowStockItems();

    /**
     * Get all inventory items that are completely out of stock.
     */
    @Query("SELECT i FROM Inventory i WHERE i.stockLevel = 0")
    List<Inventory> findOutOfStockItems();

    /**
     * Count of low-stock items.
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.stockLevel <= i.lowStockThreshold AND i.stockLevel > 0")
    Long countLowStockItems();

    /**
     * Count of out-of-stock items.
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.stockLevel = 0")
    Long countOutOfStockItems();
}
