package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.CategoryCreateRequest;
import com.ooadproject.backend.dto.CategoryDTO;
import com.ooadproject.backend.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Integer categoryId) {
        CategoryDTO category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/with-products")
    public ResponseEntity<List<CategoryDTO>> getCategoriesWithProducts() {
        List<CategoryDTO> categories = categoryService.getCategoriesWithProducts();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategories(@RequestParam String name) {
        List<CategoryDTO> categories = categoryService.searchCategories(name);
        return ResponseEntity.ok(categories);
    }

    // Admin endpoints
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        try {
            CategoryDTO category = categoryService.createCategory(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Category created successfully",
                    "data", category
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/admin/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody CategoryCreateRequest request) {
        try {
            CategoryDTO category = categoryService.updateCategory(categoryId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Category updated successfully",
                    "data", category
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/admin/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Category deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}