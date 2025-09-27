package com.ooadproject.backend.repositories;

import com.ooadproject.backend.entities.Cart;
import com.ooadproject.backend.entities.CartItem;
import com.ooadproject.backend.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}