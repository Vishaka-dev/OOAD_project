-- Personalization Options Table (Member B)
CREATE TABLE personalization_options (
    option_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    usi_type VARCHAR(50),
    massage VARCHAR(100), -- Massage
    color ENUM ('Red', 'Purple', 'White', 'Yello') DEFAULT 'Red',
    extra_price DECIMAL(10,2) DEFAULT 0.00,
    max_length INT DEFAULT 20, -- If length is, exceed the 20 char change it
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);
