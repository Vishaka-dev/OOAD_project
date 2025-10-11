-- Add denormalized personalization columns to cart_items for easier reporting
ALTER TABLE cart_items
    ADD COLUMN customization_id VARCHAR(100) NULL,
    ADD COLUMN occasion VARCHAR(100) NULL AFTER quantity,
    ADD COLUMN teddy VARCHAR(50) NULL AFTER occasion,
    ADD COLUMN teddy_type VARCHAR(100) NULL AFTER teddy,
    ADD COLUMN teddy_color VARCHAR(50) NULL AFTER teddy_type,
    ADD COLUMN flowers_count INT NULL AFTER teddy_color,
    ADD COLUMN flowers_color VARCHAR(50) NULL AFTER flowers_count,
    ADD COLUMN wrapping_paper VARCHAR(50) NULL AFTER flowers_color,
    ADD COLUMN soft_toys VARCHAR(50) NULL AFTER wrapping_paper,
    ADD COLUMN felt_design VARCHAR(255) NULL AFTER soft_toys;

ALTER TABLE order_items ADD COLUMN customization_id VARCHAR(100) NULL;