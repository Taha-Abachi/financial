-- Add item_category_limited column to gift_card table
ALTER TABLE gift_card
ADD COLUMN item_category_limited BOOLEAN DEFAULT FALSE;

-- Create join table for gift card and allowed item categories
CREATE TABLE giftcard_item_category_limitation (
    giftcard_id BIGINT NOT NULL,
    item_category_id BIGINT NOT NULL,
    PRIMARY KEY (giftcard_id, item_category_id),
    CONSTRAINT fk_giftcard_item_category
        FOREIGN KEY (giftcard_id)
        REFERENCES gift_card(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_giftcard_item_category_ref
        FOREIGN KEY (item_category_id)
        REFERENCES item_category(id)
        ON DELETE CASCADE
); 