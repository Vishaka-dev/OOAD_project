package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.CartItemDTO;
import com.ooadproject.backend.entities.User;
import com.ooadproject.backend.services.CartService;
import com.ooadproject.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCartItems(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItemDTO> items = cartService.getCartItems(user);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getCartTotal(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal total = cartService.getCartTotal(user);
        return ResponseEntity.ok(total);
    }

//    @PostMapping("/add")
//    public ResponseEntity<?> addToCart(
//            @RequestParam Integer productId,
//            @RequestParam Integer quantity,
//            @RequestBody(required = false) Map<String, Object> personalizationDetails,
//            Authentication authentication) {
//        try {
//            User user = userService.findByUsername(authentication.getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            cartService.addToCart(user, productId, quantity, personalizationDetails);
//            return ResponseEntity.ok("Item added to cart successfully");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestParam Integer productId,
            @RequestParam Integer quantity,
            @RequestBody(required = false) Map<String, Object> personalizationDetails,
            Authentication authentication) {
        try {
            User user = null;
            if (authentication != null) {
                user = userService.findByUsername(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else {
                return ResponseEntity.badRequest().body("Authentication required to add items to cart");
            }

            cartService.addToCart(user, productId, quantity, personalizationDetails);
            return ResponseEntity.ok("Item added to cart successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/update/{itemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Integer itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            cartService.updateCartItem(user, itemId, quantity);
            return ResponseEntity.ok("Cart item updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Integer itemId,
            Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            cartService.removeFromCart(user, itemId);
            return ResponseEntity.ok("Item removed from cart successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            cartService.clearCart(user);
            return ResponseEntity.ok("Cart cleared successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

