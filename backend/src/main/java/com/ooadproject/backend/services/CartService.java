package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.CartItemDTO;
import com.ooadproject.backend.dto.PersonalizationDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // In-memory cart store for unauthenticated users (session-based)
    // Using static to persist across requests in stateless application
    private static final Map<String, List<CartItemDTO>> sessionCarts = new ConcurrentHashMap<>();

    @Transactional
    public Cart getOrCreateCart(User user) {
        if (user == null) {
            // For unauthenticated users, create a temporary cart
            Cart cart = new Cart();
            cart.setUser(null); // Allow null user for temporary carts
            return cartRepository.save(cart);
        }
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public CartItem addToCart(User user, Integer productId, Integer quantity,
            PersonalizationDTO personalizationDTO) {
        if (user == null) {
            // For unauthenticated users, use a default session ID
            String sessionId = "anonymous";
            return addToSessionCart(sessionId, productId, quantity, personalizationDTO);
        }

        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        // Convert PersonalizationDTO to Map for JSON storage
        Map<String, Object> personalizationMap = null;
        if (personalizationDTO != null) {
            // Generate customization ID if not provided
            if (personalizationDTO.getCustomizationId() == null) {
                personalizationDTO.setCustomizationId(PersonalizationDTO.generateCustomizationId());
            }
            // Calculate and set extra cost
            personalizationDTO.setExtraCost(personalizationDTO.calculateExtraCost());
            personalizationMap = personalizationDTO.toMap();
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPersonalizationDetails(personalizationMap);
            return cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPersonalizationDetails(personalizationMap);
            return cartItemRepository.save(newItem);
        }
    }

    private CartItem addToSessionCart(String sessionId, Integer productId, Integer quantity,
            PersonalizationDTO personalizationDTO) {
        System.out.println("🔄 Adding to session cart: sessionId=" + sessionId + ", productId=" + productId
                + ", quantity=" + quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        List<CartItemDTO> cartItems = sessionCarts.computeIfAbsent(sessionId, k -> new ArrayList<>());
        System.out.println("🔄 Current cart items count: " + cartItems.size());

        // Convert PersonalizationDTO to Map for JSON storage
        Map<String, Object> personalizationMap = null;
        if (personalizationDTO != null) {
            // Generate customization ID if not provided
            if (personalizationDTO.getCustomizationId() == null) {
                personalizationDTO.setCustomizationId(PersonalizationDTO.generateCustomizationId());
            }
            // Calculate and set extra cost
            personalizationDTO.setExtraCost(personalizationDTO.calculateExtraCost());
            personalizationMap = personalizationDTO.toMap();
        }

        // Check if item already exists
        Optional<CartItemDTO> existingItem = cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemDTO item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPersonalizationDetails(personalizationMap);

            // Calculate extra price and total
            BigDecimal extraPrice = calculateExtraPrice(personalizationMap);
            item.setExtraPrice(extraPrice);
            BigDecimal baseTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal extraTotal = extraPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setItemTotal(baseTotal.add(extraTotal));

            System.out.println("🔄 Updated existing item: " + item);
        } else {
            CartItemDTO newItem = new CartItemDTO();
            newItem.setItemId(cartItems.size() + 1); // Simple ID generation
            newItem.setProductId(productId);
            newItem.setProductName(product.getName());
            newItem.setProductPrice(product.getPrice());
            newItem.setImageUrl(product.getImageUrl());
            newItem.setQuantity(quantity);
            newItem.setPersonalizationDetails(personalizationMap);

            // Calculate extra price and total
            BigDecimal extraPrice = calculateExtraPrice(personalizationMap);
            newItem.setExtraPrice(extraPrice);
            BigDecimal baseTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal extraTotal = extraPrice.multiply(BigDecimal.valueOf(quantity));
            newItem.setItemTotal(baseTotal.add(extraTotal));

            cartItems.add(newItem);
            System.out.println("🔄 Added new item: " + newItem);
        }

        System.out.println("🔄 Final cart items count: " + cartItems.size());
        System.out.println("🔄 Session carts keys: " + sessionCarts.keySet());

        // Return a dummy CartItem for compatibility
        CartItem dummyItem = new CartItem();
        dummyItem.setItemId(cartItems.size());
        dummyItem.setQuantity(quantity);
        return dummyItem;
    }

    @Transactional
    public void updateCartItem(User user, Integer itemId, Integer quantity) {
        if (user == null) {
            updateSessionCartItem("anonymous", itemId, quantity);
            return;
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Check authorization only if user is not null
        if (user != null && !item.getCart().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized access");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    private void updateSessionCartItem(String sessionId, Integer itemId, Integer quantity) {
        List<CartItemDTO> cartItems = sessionCarts.get(sessionId);
        if (cartItems == null) {
            throw new RuntimeException("Cart not found");
        }

        Optional<CartItemDTO> itemOpt = cartItems.stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst();

        if (itemOpt.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItemDTO item = itemOpt.get();
        if (quantity <= 0) {
            cartItems.remove(item);
        } else {
            item.setQuantity(quantity);
            // Recalculate total including extra price
            BigDecimal baseTotal = item.getProductPrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal extraTotal = item.getExtraPrice().multiply(BigDecimal.valueOf(quantity));
            item.setItemTotal(baseTotal.add(extraTotal));
        }
    }

    @Transactional
    public void removeFromCart(User user, Integer itemId) {
        if (user == null) {
            removeFromSessionCart("anonymous", itemId);
            return;
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Check authorization only if user is not null
        if (user != null && !item.getCart().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized access");
        }

        cartItemRepository.delete(item);
    }

    private void removeFromSessionCart(String sessionId, Integer itemId) {
        List<CartItemDTO> cartItems = sessionCarts.get(sessionId);
        if (cartItems == null) {
            throw new RuntimeException("Cart not found");
        }

        cartItems.removeIf(item -> item.getItemId().equals(itemId));
    }

    public List<CartItemDTO> getCartItems(User user) {
        if (user == null) {
            System.out.println("🔄 Getting cart items for anonymous user");
            List<CartItemDTO> items = sessionCarts.getOrDefault("anonymous", new ArrayList<>());
            System.out.println("🔄 Found " + items.size() + " items in session cart");
            System.out.println("🔄 Session carts keys: " + sessionCarts.keySet());
            return items;
        }

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
        if (user == null) {
            sessionCarts.remove("anonymous");
            return;
        }

        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAll(cart.getCartItems());
    }

    @Transactional
    public void createNewCart(User user) {
        if (user == null) {
            // For unauthenticated users, clear the session cart
            sessionCarts.remove("anonymous");
            System.out.println("🔄 Created new session cart for anonymous user");
            return;
        }

        // For authenticated users, delete the existing cart and its items
        Cart existingCart = cartRepository.findByUser(user).orElse(null);
        if (existingCart != null) {
            cartItemRepository.deleteAll(existingCart.getCartItems());
            cartRepository.delete(existingCart);
            System.out.println("🔄 Deleted existing cart for user: " + user.getUsername());
        }

        // A new cart will be created automatically on the next addToCart call
        System.out.println("🔄 New cart will be created on next addToCart call for user: " + user.getUsername());
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

        // Calculate extra price based on personalization details
        BigDecimal extraPrice = calculateExtraPrice(item.getPersonalizationDetails());
        dto.setExtraPrice(extraPrice);

        // Calculate total including extra price
        BigDecimal baseTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal extraTotal = extraPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        dto.setItemTotal(baseTotal.add(extraTotal));

        return dto;
    }

    private BigDecimal calculateExtraPrice(Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null || personalizationDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Use the PersonalizationDTO calculation method for consistency
        PersonalizationDTO dto = PersonalizationDTO.fromMap(personalizationDetails);
        if (dto != null) {
            return dto.calculateExtraCost();
        }

        // Fallback to direct calculation if DTO conversion fails
        BigDecimal extraPrice = BigDecimal.ZERO;

        // Occasion pricing
        String occasion = (String) personalizationDetails.get("occasion");
        if (occasion != null) {
            switch (occasion) {
                case "Graduation" -> extraPrice = extraPrice.add(BigDecimal.valueOf(5));
                case "Birthday" -> extraPrice = extraPrice.add(BigDecimal.valueOf(3));
                case "Valentine" -> extraPrice = extraPrice.add(BigDecimal.valueOf(8));
                case "Mini" -> extraPrice = extraPrice.add(BigDecimal.valueOf(2));
            }
        }

        // Teddy pricing - check nested teddy object
        Object teddyObj = personalizationDetails.get("teddy");
        if (teddyObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> teddyMap = (Map<String, Object>) teddyObj;

            Boolean included = (Boolean) teddyMap.get("included");
            if (Boolean.TRUE.equals(included)) {
                extraPrice = extraPrice.add(BigDecimal.valueOf(15));

                String teddyType = (String) teddyMap.get("type");
                if (teddyType != null) {
                    switch (teddyType) {
                        case "handmade" -> extraPrice = extraPrice.add(BigDecimal.valueOf(5));
                        case "fluffy" -> extraPrice = extraPrice.add(BigDecimal.valueOf(10));
                    }
                }
            }
        }

        // Flowers pricing - check nested flowers object
        Object flowersObj = personalizationDetails.get("flowers");
        if (flowersObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> flowersMap = (Map<String, Object>) flowersObj;

            Object countObj = flowersMap.get("count");
            if (countObj instanceof Number) {
                extraPrice = extraPrice.add(BigDecimal.valueOf(((Number) countObj).intValue()));
            }
        }

        // Wrapping paper pricing
        String wrappingPaper = (String) personalizationDetails.get("wrapping_paper");
        if (wrappingPaper != null) {
            switch (wrappingPaper) {
                case "Premium" -> extraPrice = extraPrice.add(BigDecimal.valueOf(3));
                case "Gift Box" -> extraPrice = extraPrice.add(BigDecimal.valueOf(5));
            }
        }

        // Soft toys pricing
        String softToys = (String) personalizationDetails.get("soft_toys");
        if ("Yes".equals(softToys)) {
            extraPrice = extraPrice.add(BigDecimal.valueOf(8));
        }

        // Custom felt design pricing
        String feltDesign = (String) personalizationDetails.get("felt_design");
        if (feltDesign != null && !feltDesign.trim().isEmpty()) {
            extraPrice = extraPrice.add(BigDecimal.valueOf(5));
        }

        return extraPrice;
    }
}