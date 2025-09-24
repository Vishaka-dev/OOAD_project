package com.ooadproject.backend.config;

import com.ooadproject.backend.entities.Product;
import com.ooadproject.backend.repositories.ProductRepository;
import com.ooadproject.backend.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public void run(String... args) throws Exception {
        // Create inventory entries for existing products
        productRepository.findAll().forEach(product -> {
            try {
                inventoryService.createInventoryForProduct(product);
                System.out.println("Created inventory for product: " + product.getName());
            } catch (Exception e) {
                System.out.println("Inventory already exists for product: " + product.getName());
            }
        });
    }
}
