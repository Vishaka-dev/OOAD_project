-- Add optional customization_id for personalized items
ALTER TABLE cart_items ADD COLUMN customization_id VARCHAR(100) NULL;
ALTER TABLE order_items ADD COLUMN customization_id VARCHAR(100) NULL;

