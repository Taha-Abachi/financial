-- Add batch_id column to gift_card table
ALTER TABLE gift_card ADD COLUMN batch_id BIGINT;

-- Add foreign key constraint to batch table
ALTER TABLE gift_card ADD CONSTRAINT fk_giftcard_batch
    FOREIGN KEY (batch_id) REFERENCES batch(id);

-- Create index for batch_id column for better query performance
CREATE INDEX idx_giftcard_batch ON gift_card(batch_id);
