package com.ooadproject.backend.repository;

import com.ooadproject.backend.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    @Query("SELECT i FROM Inventory i WHERE i.stockLevel <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i JOIN i.product p WHERE p.categoryId = :categoryId")
    List<Inventory> findByCategoryId(Integer categoryId);

    @Query("SELECT i FROM Inventory i WHERE i.stockLevel = 0")
    List<Inventory> findOutOfStockItems();
}
