-- Seed default category
INSERT INTO categories (category_id, name, description)
VALUES (1, 'Default', 'Default category')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- Seed sample products with fixed IDs matching frontend expectations
INSERT INTO products (product_id, category_id, name, description, price, image_url, stock_quantity) VALUES
(1, 1, 'Classic Brown Teddy', 'A timeless brown teddy bear perfect for cuddling', 29.99, '/placeholder.svg', 15),
(2, 1, 'Pink Princess Bear', 'Adorable pink teddy with a sparkly crown', 34.99, '/placeholder.svg', 8),
(3, 1, 'Tiny Pocket Bear', 'Perfect small companion for on-the-go adventures', 12.99, '/placeholder.svg', 25),
(4, 1, 'Giant Cuddle Bear', 'Extra large teddy for the ultimate cuddle experience', 89.99, '/placeholder.svg', 3),
(5, 1, 'Cream Vanilla Bear', 'Soft cream-colored teddy with vanilla scent', 27.99, '/placeholder.svg', 12),
(6, 1, 'Adventure Explorer Bear', 'Comes with hat and backpack for adventures', 42.99, '/placeholder.svg', 6)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), price = VALUES(price), image_url = VALUES(image_url), stock_quantity = VALUES(stock_quantity);

-- Optional: seed one personalization option for product 1
-- Adjust color to match enum in V2__personalization.sql (case-sensitive)
INSERT INTO personalization_options (product_id, usi_type, massage, color, extra_price, max_length)
VALUES (1, 'Engraving', 'Congrats!', 'Red', 2.50, 30)
ON DUPLICATE KEY UPDATE usi_type = VALUES(usi_type), massage = VALUES(massage), color = VALUES(color), extra_price = VALUES(extra_price), max_length = VALUES(max_length);


