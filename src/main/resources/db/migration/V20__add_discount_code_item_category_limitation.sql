-- Add item_category_limited column to discount_code table
ALTER TABLE discount_code
ADD COLUMN item_category_limited BOOLEAN DEFAULT FALSE;

-- Create join table for discount code and allowed item categories
CREATE TABLE discountcode_item_category_limitation (
    discountcode_id BIGINT NOT NULL,
    item_category_id BIGINT NOT NULL,
    PRIMARY KEY (discountcode_id, item_category_id),
    CONSTRAINT fk_discountcode_item_category
        FOREIGN KEY (discountcode_id)
        REFERENCES discount_code(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_category
        FOREIGN KEY (item_category_id)
        REFERENCES item_category(id)
        ON DELETE CASCADE
); 