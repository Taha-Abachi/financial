-- Add discount type column to discount_code table
ALTER TABLE discount_code ADD COLUMN discount_type VARCHAR(20) NOT NULL DEFAULT 'PERCENTAGE';

-- Add comment to explain the column
COMMENT ON COLUMN discount_code.discount_type IS 'Type of discount: PERCENTAGE for percentage-based discount, CONSTANT for fixed amount discount.';

-- Add check constraint to ensure valid values
ALTER TABLE discount_code ADD CONSTRAINT chk_discount_type CHECK (discount_type IN ('PERCENTAGE', 'CONSTANT')); 