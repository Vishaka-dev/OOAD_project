-- Inventory Table (Member D)
CREATE TABLE inventory (
    product_id INT PRIMARY KEY,
    stock_level INT DEFAULT 0,
    low_stock_threshold INT DEFAULT 10,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- Delivery Slots Table (Member D)
CREATE TABLE delivery_slots (
    slot_id SERIAL PRIMARY KEY,
    order_id INT,
    delivery_date DATE NOT NULL,
    time_slot VARCHAR(50),
    courier_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'assigned', 'in_transit', 'delivered')),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- Index (Member D)
CREATE INDEX idx_delivery_slots_order ON delivery_slots(order_id);