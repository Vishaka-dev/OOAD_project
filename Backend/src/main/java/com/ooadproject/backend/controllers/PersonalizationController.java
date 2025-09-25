package com.ooadproject.backend.controllers;


import com.ooadproject.backend.dto.PersonalizationOptionDTO;
import com.ooadproject.backend.dto.AddPersonalizedToCartRequest;
import com.ooadproject.backend.dto.CartItemDTO;
import com.ooadproject.backend.entities.CartItem;
import com.ooadproject.backend.entities.User;
import com.ooadproject.backend.services.UserService;
import com.ooadproject.backend.services.PersonalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personalization")
@RequiredArgsConstructor
public class PersonalizationController {

    private final PersonalizationService personalizationService;
    private final UserService userService;

    @GetMapping("/products/{productId}/options")
    public ResponseEntity<List<PersonalizationOptionDTO>> getOptionsByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(personalizationService.getOptionsByProduct(productId));
    }

    @PostMapping("/products/{productId}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonalizationOptionDTO> createOption(
            @PathVariable Integer productId,
            @RequestBody PersonalizationOptionDTO request
    ) {
        return ResponseEntity.ok(personalizationService.createOption(productId, request));
    }

    @PutMapping("/options/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonalizationOptionDTO> updateOption(
            @PathVariable Integer optionId,
            @RequestBody PersonalizationOptionDTO request
    ) {
        return ResponseEntity.ok(personalizationService.updateOption(optionId, request));
    }

    @DeleteMapping("/options/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteOption(@PathVariable Integer optionId) {
        personalizationService.deleteOption(optionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<CartItemDTO> addPersonalizedToCart(
            @RequestBody AddPersonalizedToCartRequest request,
            org.springframework.security.core.Authentication authentication
    ) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        CartItem item = personalizationService.createOptionAndAddToCart(user, request);
        CartItemDTO dto = new CartItemDTO();
        dto.setItemId(item.getItemId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductPrice(item.getProduct().getPrice());
        dto.setImageUrl(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setPersonalizationDetails(item.getPersonalizationDetails());
        dto.setCustomizationId(item.getCustomizationId());
        dto.setItemTotal(item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
        return ResponseEntity.ok(dto);
    }

}
