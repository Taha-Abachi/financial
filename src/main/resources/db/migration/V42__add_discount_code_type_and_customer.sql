-- Add type column to discount_code table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'discount_code' AND column_name = 'type'
    ) THEN
        ALTER TABLE discount_code ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'GENERAL';
    END IF;
END $$;

-- Update all existing discount codes to GENERAL type (if any are NULL or empty)
UPDATE discount_code SET type = 'GENERAL' WHERE type IS NULL OR type = '';

-- Add customer_id column to discount_code table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'discount_code' AND column_name = 'customer_id'
    ) THEN
        ALTER TABLE discount_code ADD COLUMN customer_id BIGINT;
        ALTER TABLE discount_code ADD CONSTRAINT fk_discount_code_customer 
            FOREIGN KEY (customer_id) REFERENCES customer(id);
    END IF;
END $$;

-- Add comment to explain the columns
COMMENT ON COLUMN discount_code.type IS 'Type of discount code: PERSONAL for customer-specific codes, GENERAL for general-use codes.';
COMMENT ON COLUMN discount_code.customer_id IS 'Customer ID for PERSONAL type discount codes. NULL for GENERAL type codes.';

-- Add check constraint to ensure valid values (if constraint doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_discount_code_type' AND table_name = 'discount_code'
    ) THEN
        ALTER TABLE discount_code ADD CONSTRAINT chk_discount_code_type CHECK (type IN ('PERSONAL', 'GENERAL'));
    END IF;
END $$;

