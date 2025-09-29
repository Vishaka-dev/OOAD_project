package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.InventoryDTO;
import com.ooadproject.backend.dto.StockUpdateRequest;
import com.ooadproject.backend.services.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAllInventory() {
        List<InventoryDTO> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDTO> getInventoryByProductId(@PathVariable Integer productId) {
        InventoryDTO inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDTO>> getLowStockItems() {
        List<InventoryDTO> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<InventoryDTO>> getOutOfStockItems() {
        List<InventoryDTO> outOfStockItems = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(outOfStockItems);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateStock(@Valid @RequestBody StockUpdateRequest request) {
        try {
            InventoryDTO inventory = inventoryService.updateStock(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Stock updated successfully",
                    "data", inventory
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{productId}/adjust")
    public ResponseEntity<?> adjustStock(
            @PathVariable Integer productId,
            @RequestParam Integer adjustment) {
        try {
            InventoryDTO inventory = inventoryService.adjustStock(productId, adjustment);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Stock adjusted successfully",
                    "data", inventory
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncInventory() {
        try {
            inventoryService.syncInventoryWithProducts();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Inventory synced with products successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getInventoryStatistics() {
        Map<String, Long> stats = Map.of(
                "lowStockCount", inventoryService.getLowStockCount(),
                "outOfStockCount", inventoryService.getOutOfStockCount()
        );
        return ResponseEntity.ok(stats);
    }
}