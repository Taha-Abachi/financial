-- Add store_limited column to gift_card table
ALTER TABLE gift_card ADD COLUMN store_limited BOOLEAN DEFAULT FALSE;

-- Create join table for gift card store limitations
CREATE TABLE giftcard_store_limitation (
    giftcard_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    PRIMARY KEY (giftcard_id, store_id),
    CONSTRAINT fk_giftcard_store_limitation_giftcard
        FOREIGN KEY (giftcard_id)
        REFERENCES gift_card (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_giftcard_store_limitation_store
        FOREIGN KEY (store_id)
        REFERENCES store (id)
        ON DELETE CASCADE
);

-- Add index for better query performance
CREATE INDEX idx_giftcard_store_limitation_store_id ON giftcard_store_limitation(store_id); 