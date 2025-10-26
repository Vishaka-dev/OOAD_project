package com.ooadproject.backend.config;

import com.ooadproject.backend.entities.Category;
import com.ooadproject.backend.entities.Product;
import com.ooadproject.backend.entities.User;
import com.ooadproject.backend.repositories.CategoryRepository;
import com.ooadproject.backend.repositories.ProductRepository;
import com.ooadproject.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🌱 Starting data seeding...");

        // Create default users if they don't exist
        if (userRepository.count() == 0) {
            createDefaultUsers();
        }

        // Create default categories if they don't exist
        if (categoryRepository.count() == 0) {
            createDefaultCategories();
        }

        // // Create default products if they don't exist
        // if (productRepository.count() == 0) {
        //     createDefaultProducts();
        // }

        System.out.println("✅ Data seeding completed!");
    }

    private void createDefaultUsers() {
        System.out.println("👤 Creating default users...");

        // Admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@giftshop.com");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(User.Role.admin);
        userRepository.save(admin);

        // Customer user
        User customer = new User();
        customer.setUsername("hirusha");
        customer.setEmail("hirusha12.silva@gmail.com");
        customer.setPasswordHash(passwordEncoder.encode("2004zipza"));
        customer.setRole(User.Role.customer);
        userRepository.save(customer);

        System.out.println("✅ Default users created");
    }

    private void createDefaultCategories() {
        System.out.println("📂 Creating default categories...");

        Category defaultCategory1 = new Category();
        defaultCategory1.setName("Teddy Bears");
        defaultCategory1.setDescription("Cute and cuddly teddy bears");
        categoryRepository.save(defaultCategory1);
        
        Category defaultCategory2 = new Category();
        defaultCategory2.setName("Teddy With Flowers");
        defaultCategory2.setDescription("Bouquet of Flowers with a Cute and cuddly teddy bear");
        categoryRepository.save(defaultCategory2);

        Category defaultCategory3 = new Category();
        defaultCategory3.setName("Flowers");
        defaultCategory3.setDescription("Bouquet of Flowers");
        categoryRepository.save(defaultCategory3);

        System.out.println("✅ Default categories created");
    }

    // private void createDefaultProducts() {
    //     System.out.println("🧸 Creating default products...");

    //     Category defaultCategory = categoryRepository.findByNameIgnoreCase("Teddy Bears")
    //             .orElse(categoryRepository.findAll().get(0));

    //     // Product 1
    //     Product product1 = new Product();
    //     product1.setName("Classic Brown Teddy");
    //     product1.setDescription("A timeless brown teddy bear perfect for cuddling");
    //     product1.setPrice(new BigDecimal("29.99"));
    //     product1.setImageUrl("/placeholder.svg");
    //     product1.setStockQuantity(15);
    //     product1.setCategory(defaultCategory);
    //     productRepository.save(product1);

    //     // Product 2
    //     Product product2 = new Product();
    //     product2.setName("Pink Princess Bear");
    //     product2.setDescription("Adorable pink teddy with a sparkly crown");
    //     product2.setPrice(new BigDecimal("34.99"));
    //     product2.setImageUrl("/placeholder.svg");
    //     product2.setStockQuantity(8);
    //     product2.setCategory(defaultCategory);
    //     productRepository.save(product2);

    //     // Product 3
    //     Product product3 = new Product();
    //     product3.setName("Tiny Pocket Bear");
    //     product3.setDescription("Perfect small companion for on-the-go adventures");
    //     product3.setPrice(new BigDecimal("12.99"));
    //     product3.setImageUrl("/placeholder.svg");
    //     product3.setStockQuantity(25);
    //     product3.setCategory(defaultCategory);
    //     productRepository.save(product3);

    //     System.out.println("✅ Default products created");
    // }
}
