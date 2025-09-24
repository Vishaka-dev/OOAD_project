package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.InventoryDTO;
import com.ooadproject.backend.entities.Inventory;
import com.ooadproject.backend.entities.Product;
import com.ooadproject.backend.repositories.InventoryRepository;
import com.ooadproject.backend.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

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

        // Also update the product's stock quantity to keep them in sync
        Product product = inventory.getProduct();
        product.setStockQuantity(newStockLevel);
        productRepository.save(product);

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

        // Also update the product's stock quantity
        Product product = inventory.getProduct();
        product.setStockQuantity(inventory.getStockLevel());
        productRepository.save(product);

        inventoryRepository.save(inventory);
    }

    public void increaseStock(Integer productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        inventory.setStockLevel(inventory.getStockLevel() + quantity);

        // Also update the product's stock quantity
        Product product = inventory.getProduct();
        product.setStockQuantity(inventory.getStockLevel());
        productRepository.save(product);

        inventoryRepository.save(inventory);
    }

    @Transactional
    public void createInventoryForProduct(Product product) {
        // Check if inventory already exists
        if (!inventoryRepository.existsById(product.getProductId())) {
            Inventory inventory = new Inventory();
            inventory.setProductId(product.getProductId());
            inventory.setProduct(product);
            inventory.setStockLevel(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
            inventory.setLowStockThreshold(10); // Default threshold
            inventoryRepository.save(inventory);
        }
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setProductId(inventory.getProductId());
        dto.setStockLevel(inventory.getStockLevel());
        dto.setLowStockThreshold(inventory.getLowStockThreshold());
        dto.setIsLowStock(inventory.getStockLevel() <= inventory.getLowStockThreshold());

        if (inventory.getProduct() != null) {
            dto.setProductName(inventory.getProduct().getName());
            dto.setPrice(BigDecimal.valueOf(inventory.getProduct().getPrice()));
            dto.setImageUrl(inventory.getProduct().getImageUrl());
            if (inventory.getProduct().getCategory() != null) {
                dto.setCategoryName(inventory.getProduct().getCategory().getName());
            }
        }

        return dto;
    }
}
