package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.InventoryDTO;
import com.ooadproject.backend.dto.StockUpdateRequest;
import com.ooadproject.backend.entities.Inventory;
import com.ooadproject.backend.entities.Product;
import com.ooadproject.backend.repositories.InventoryRepository;
import com.ooadproject.backend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public List<InventoryDTO> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public InventoryDTO getInventoryByProductId(Integer productId) {
        Inventory inventory = inventoryRepository.findByProductProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        return convertToDTO(inventory);
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

    @Transactional
    public InventoryDTO updateStock(StockUpdateRequest request) {
        // Get or create inventory record
        Inventory inventory = inventoryRepository.findByProductProductId(request.getProductId())
                .orElseGet(() -> {
                    Product product = productRepository.findById(request.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));

                    Inventory newInventory = new Inventory();
                    newInventory.setProductId(request.getProductId());
                    newInventory.setProduct(product);
                    return newInventory;
                });

        // Update inventory
        inventory.setStockLevel(request.getStockLevel());
        inventory.setLowStockThreshold(request.getLowStockThreshold());

        // Also update product stock quantity to keep in sync
        Product product = inventory.getProduct();
        product.setStockQuantity(request.getStockLevel());
        productRepository.save(product);

        inventory = inventoryRepository.save(inventory);
        return convertToDTO(inventory);
    }

    @Transactional
    public InventoryDTO adjustStock(Integer productId, Integer adjustment) {
        Inventory inventory = inventoryRepository.findByProductProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        int newStockLevel = Math.max(0, inventory.getStockLevel() + adjustment);
        inventory.setStockLevel(newStockLevel);

        // Update product stock quantity
        Product product = inventory.getProduct();
        product.setStockQuantity(newStockLevel);
        productRepository.save(product);

        inventory = inventoryRepository.save(inventory);
        return convertToDTO(inventory);
    }

    public Long getLowStockCount() {
        return inventoryRepository.countLowStockItems();
    }

    public Long getOutOfStockCount() {
        return inventoryRepository.countOutOfStockItems();
    }

    @Transactional
    public void syncInventoryWithProducts() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            Inventory inventory = inventoryRepository.findByProductProductId(product.getProductId())
                    .orElseGet(() -> {
                        Inventory newInventory = new Inventory();
                        newInventory.setProductId(product.getProductId());
                        newInventory.setProduct(product);
                        newInventory.setLowStockThreshold(10);
                        return newInventory;
                    });

            inventory.setStockLevel(product.getStockQuantity());
            inventoryRepository.save(inventory);
        }
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setProductId(inventory.getProductId());
        dto.setProductName(inventory.getProduct().getName());
        dto.setCategoryName(inventory.getProduct().getCategory() != null ?
                inventory.getProduct().getCategory().getName() : null);
        dto.setStockLevel(inventory.getStockLevel());
        dto.setLowStockThreshold(inventory.getLowStockThreshold());
        dto.setIsLowStock(inventory.isLowStock());
        dto.setIsOutOfStock(inventory.isOutOfStock());
        dto.setLastUpdated(inventory.getUpdatedAt());
        return dto;
    }
}