-- Add store_limited column to discount_code table
ALTER TABLE discount_code
ADD COLUMN store_limited BOOLEAN DEFAULT FALSE;

-- Create join table for discount code and allowed stores
CREATE TABLE discountcode_store_limitation (
    discountcode_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    PRIMARY KEY (discountcode_id, store_id),
    CONSTRAINT fk_discountcode
        FOREIGN KEY (discountcode_id)
        REFERENCES discount_code(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_store
        FOREIGN KEY (store_id)
        REFERENCES store(id)
        ON DELETE CASCADE
); 