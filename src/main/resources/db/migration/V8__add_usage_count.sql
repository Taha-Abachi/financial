-- Add usage limit and current usage count columns to discount_code table
ALTER TABLE discount_code ADD COLUMN usage_limit INTEGER NOT NULL DEFAULT 1;
ALTER TABLE discount_code ADD COLUMN current_usage_count INTEGER NOT NULL DEFAULT 0;

-- Add comments to explain the columns
COMMENT ON COLUMN discount_code.usage_limit IS 'Maximum number of times this discount code can be used. Default is 1.';
COMMENT ON COLUMN discount_code.current_usage_count IS 'Current number of times this discount code has been used.'; 