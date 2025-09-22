package com.ooadproject.backend.service;

import com.ooadproject.backend.dto.InventoryDTO;
import com.ooadproject.backend.entity.Inventory;
import com.ooadproject.backend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<InventoryDTO> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InventoryDTO> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InventoryDTO> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<InventoryDTO> getInventoryByProductId(Integer productId) {
        return inventoryRepository.findById(productId)
                .map(this::convertToDTO);
    }

    public InventoryDTO updateStock(Integer productId, Integer newStockLevel) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        inventory.setStockLevel(newStockLevel);
        Inventory updated = inventoryRepository.save(inventory);
        return convertToDTO(updated);
    }

    public InventoryDTO updateLowStockThreshold(Integer productId, Integer threshold) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        inventory.setLowStockThreshold(threshold);
        Inventory updated = inventoryRepository.save(inventory);
        return convertToDTO(updated);
    }

    public void decreaseStock(Integer productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        if (inventory.getStockLevel() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }

        inventory.setStockLevel(inventory.getStockLevel() - quantity);
        inventoryRepository.save(inventory);
    }

    public void increaseStock(Integer productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        inventory.setStockLevel(inventory.getStockLevel() + quantity);
        inventoryRepository.save(inventory);
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setProductId(inventory.getProductId());
        dto.setStockLevel(inventory.getStockLevel());
        dto.setLowStockThreshold(inventory.getLowStockThreshold());
        dto.setIsLowStock(inventory.getStockLevel() <= inventory.getLowStockThreshold());

        if (inventory.getProduct() != null) {
            dto.setProductName(inventory.getProduct().getName());
            dto.setPrice(inventory.getProduct().getPrice().doubleValue());
            dto.setImageUrl(inventory.getProduct().getImageUrl());
            // Assuming you have category relationship
            // dto.setCategoryName(inventory.getProduct().getCategory().getName());
        }

        return dto;
    }
}