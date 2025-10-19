-- enforce non-negative price (MySQL 8+ supports CHECK)
ALTER TABLE products
    MODIFY price DECIMAL(10,2) NOT NULL,
    ADD CONSTRAINT chk_products_price_nonneg CHECK (price >= 0.00);
