-- Add minimum_bill_amount column to discount_code table
ALTER TABLE discount_code ADD COLUMN minimum_bill_amount BIGINT NOT NULL DEFAULT 0;

-- Add comment to explain the column
COMMENT ON COLUMN discount_code.minimum_bill_amount IS 'Minimum bill amount required to apply the discount. If 0, no minimum amount is required.'; 