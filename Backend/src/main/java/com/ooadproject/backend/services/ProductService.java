package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.ProductCreateRequest;
import com.ooadproject.backend.dto.ProductDTO;
import com.ooadproject.backend.entities.CartItem;
import com.ooadproject.backend.entities.Category;
import com.ooadproject.backend.entities.Product;
import com.ooadproject.backend.repositories.CategoryRepository;
import com.ooadproject.backend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return convertToDTO(product);
    }

    public List<ProductDTO> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategoryCategoryIdOrderByNameAsc(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.searchByNameIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getInStockProducts() {
        return productRepository.findInStockProducts()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetweenOrderByPriceAsc(minPrice, maxPrice)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductDTO> searchProducts(String name, Integer categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, Boolean inStock, Pageable pageable) {
        return productRepository.searchProducts(name, categoryId, minPrice, maxPrice, inStock, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public ProductDTO createProduct(ProductCreateRequest request) {
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        Product product = new Product();
        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setStockQuantity(request.getStockQuantity());

        product = productRepository.save(product);
        return convertToDTO(product);
    }

    @Transactional
    public ProductDTO updateProduct(Integer productId, ProductCreateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setStockQuantity(request.getStockQuantity());

        product = productRepository.save(product);
        return convertToDTO(product);
    }

    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        productRepository.delete(product);
    }

    @Transactional
    public ProductDTO updateStock(Integer productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        product.setStockQuantity(newStock);
        product = productRepository.save(product);
        return convertToDTO(product);
    }

    @Transactional
    public void decrementStockForOrder(List<CartItem> cartItems) {
        System.out.println("🔄 Decrementing stock for " + cartItems.size() + " cart items");

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer quantityToDecrement = cartItem.getQuantity();
            Integer currentStock = product.getStockQuantity();

            System.out.println("🔄 Product: " + product.getName() +
                    ", Current Stock: " + currentStock +
                    ", Decrementing: " + quantityToDecrement);

            if (currentStock < quantityToDecrement) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + currentStock + ", Required: " + quantityToDecrement);
            }

            Integer newStock = currentStock - quantityToDecrement;
            product.setStockQuantity(newStock);
            productRepository.save(product);

            System.out.println("✅ Stock updated for " + product.getName() +
                    ": " + currentStock + " -> " + newStock);
        }

        System.out.println("✅ All stock quantities updated successfully");
    }

    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null);
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setInStock(product.isInStock());
        return dto;
    }
}