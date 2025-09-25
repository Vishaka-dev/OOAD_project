package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.CartItemDTO;
import com.ooadproject.backend.entities.Cart;
import com.ooadproject.backend.entities.CartItem;
import com.ooadproject.backend.entities.Product;
import com.ooadproject.backend.entities.User;
import com.ooadproject.backend.repositories.CartItemRepository;
import com.ooadproject.backend.repositories.CartRepository;
import com.ooadproject.backend.repositories.ProductRepository;
import com.ooadproject.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public CartItem addToCart(User user, Integer productId, Integer quantity,
                              Map<String, Object> personalizationDetails) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPersonalizationDetails(personalizationDetails);
            if (personalizationDetails != null && !personalizationDetails.isEmpty()) {
                item.setCustomizationId(generateCustomizationId(user.getUserId(), productId));
                copyDenormalizedPersonalization(item, personalizationDetails);
            }
            return cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPersonalizationDetails(personalizationDetails);
            if (personalizationDetails != null && !personalizationDetails.isEmpty()) {
                newItem.setCustomizationId(generateCustomizationId(user.getUserId(), productId));
                copyDenormalizedPersonalization(newItem, personalizationDetails);
            }
            return cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void updateCartItem(User user, Integer itemId, Integer quantity) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized access");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeFromCart(User user, Integer itemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized access");
        }

        cartItemRepository.delete(item);
    }

    public List<CartItemDTO> getCartItems(User user) {
        Cart cart = getOrCreateCart(user);
        return cart.getCartItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal getCartTotal(User user) {
        return getCartItems(user).stream()
                .map(CartItemDTO::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAll(cart.getCartItems());
    }

    private CartItemDTO convertToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setItemId(item.getItemId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductPrice(item.getProduct().getPrice());
        dto.setImageUrl(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setPersonalizationDetails(item.getPersonalizationDetails());
        dto.setCustomizationId(item.getCustomizationId());
        dto.setItemTotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return dto;
    }

    private String generateCustomizationId(Integer userId, Integer productId) {
        return "CUST-" + userId + "-" + productId + "-" + System.currentTimeMillis();
    }

    private void copyDenormalizedPersonalization(CartItem item, Map<String, Object> details) {
        // Safely copy common fields if present. Keys must match frontend payload.
        putString(details, "occasion").ifPresent(v -> setField(item, "occasion", v));
        putString(details, "teddy").ifPresent(v -> setField(item, "teddy", v));
        putString(details, "teddyType").ifPresent(v -> setField(item, "teddy_type", v));
        putString(details, "teddyColor").ifPresent(v -> setField(item, "teddy_color", v));
        putString(details, "flowersColor").ifPresent(v -> setField(item, "flowers_color", v));
        putString(details, "wrappingPaper").ifPresent(v -> setField(item, "wrapping_paper", v));
        putString(details, "softToys").ifPresent(v -> setField(item, "soft_toys", v));
        putString(details, "feltDesign").ifPresent(v -> setField(item, "felt_design", v));

        // flowersCount might be numeric or string
        Object count = details.get("flowersCount");
        if (count != null) {
            Integer intCount = null;
            if (count instanceof Number) intCount = ((Number) count).intValue();
            else {
                try { intCount = Integer.parseInt(String.valueOf(count)); } catch (Exception ignored) {}
            }
            if (intCount != null) setField(item, "flowers_count", intCount);
        }
    }

    private java.util.Optional<String> putString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return java.util.Optional.empty();
        String s = String.valueOf(v);
        if (s.isBlank()) return java.util.Optional.empty();
        return java.util.Optional.of(s);
    }

    private void setField(CartItem item, String columnLikeName, Object value) {
        // Use JPA entity setters if you add fields to CartItem entity; for now, execute a lightweight update via repository if needed.
        // Simpler approach: rely on JPA @DynamicUpdate if added fields exist on entity.
        try {
            java.lang.reflect.Field f;
            switch (columnLikeName) {
                case "occasion": f = CartItem.class.getDeclaredField("occasion"); break;
                case "teddy": f = CartItem.class.getDeclaredField("teddy"); break;
                case "teddy_type": f = CartItem.class.getDeclaredField("teddyType"); break;
                case "teddy_color": f = CartItem.class.getDeclaredField("teddyColor"); break;
                case "flowers_count": f = CartItem.class.getDeclaredField("flowersCount"); break;
                case "flowers_color": f = CartItem.class.getDeclaredField("flowersColor"); break;
                case "wrapping_paper": f = CartItem.class.getDeclaredField("wrappingPaper"); break;
                case "soft_toys": f = CartItem.class.getDeclaredField("softToys"); break;
                case "felt_design": f = CartItem.class.getDeclaredField("feltDesign"); break;
                default: return;
            }
            f.setAccessible(true);
            f.set(item, value);
        } catch (Exception ignored) {}
    }
}