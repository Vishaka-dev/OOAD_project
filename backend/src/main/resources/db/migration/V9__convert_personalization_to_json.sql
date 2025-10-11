-- Convert personalization from individual columns to JSON approach
-- Remove individual personalization columns from cart_items table
ALTER TABLE cart_items
    DROP COLUMN customization_id,
    DROP COLUMN occasion,
    DROP COLUMN teddy,
    DROP COLUMN teddy_type,
    DROP COLUMN teddy_color,
    DROP COLUMN flowers_count,
    DROP COLUMN flowers_color,
    DROP COLUMN wrapping_paper,
    DROP COLUMN soft_toys,
    DROP COLUMN felt_design;

-- Remove customization_id from order_items table (personalization_details is already JSON)
ALTER TABLE order_items DROP COLUMN customization_id;

-- Ensure personalization_details columns are properly configured as JSON
ALTER TABLE cart_items MODIFY COLUMN personalization_details JSON;
ALTER TABLE order_items MODIFY COLUMN personalization_details JSON;
