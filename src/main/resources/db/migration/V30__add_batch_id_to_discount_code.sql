-- Add batch_id column to discount_code table
ALTER TABLE discount_code ADD COLUMN batch_id BIGINT;

-- Add foreign key constraint to batch table
ALTER TABLE discount_code ADD CONSTRAINT fk_discountcode_batch
    FOREIGN KEY (batch_id) REFERENCES batch(id);

-- Create index for batch_id column for better query performance
CREATE INDEX idx_discountcode_batch ON discount_code(batch_id);
