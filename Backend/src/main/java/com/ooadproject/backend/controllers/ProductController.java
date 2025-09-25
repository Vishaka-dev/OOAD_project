package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.ProductResponseDTO;
import com.ooadproject.backend.entities.Product;
import com.ooadproject.backend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponseDTO> dtos = products.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private ProductResponseDTO toDto(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : "Unknown");
        dto.setStockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
        return dto;
    }
}


