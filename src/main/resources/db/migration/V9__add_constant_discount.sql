-- Add constant discount amount column to discount_code table
ALTER TABLE discount_code ADD COLUMN constant_discount_amount BIGINT NOT NULL DEFAULT 0;

-- Add comment to explain the column
COMMENT ON COLUMN discount_code.constant_discount_amount IS 'Fixed discount amount to be applied. If greater than 0, this amount will be used instead of percentage-based calculation.'; 