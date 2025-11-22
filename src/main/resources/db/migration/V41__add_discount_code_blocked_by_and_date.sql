-- Add blocked_by_user_id column to discount_code table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'discount_code' AND column_name = 'blocked_by_user_id'
    ) THEN
        ALTER TABLE discount_code ADD COLUMN blocked_by_user_id BIGINT;
        ALTER TABLE discount_code ADD CONSTRAINT fk_discount_code_blocked_by_user 
            FOREIGN KEY (blocked_by_user_id) REFERENCES users(id);
    END IF;
END $$;

-- Add blocked_date column to discount_code table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'discount_code' AND column_name = 'blocked_date'
    ) THEN
        ALTER TABLE discount_code ADD COLUMN blocked_date TIMESTAMP;
    END IF;
END $$;

-- Add blocked column to discount_code table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'discount_code' AND column_name = 'blocked'
    ) THEN
        ALTER TABLE discount_code ADD COLUMN blocked BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- Add comment to explain the columns
COMMENT ON COLUMN discount_code.blocked_by_user_id IS 'User who blocked this discount code';
COMMENT ON COLUMN discount_code.blocked_date IS 'Date and time when the discount code was blocked';
COMMENT ON COLUMN discount_code.blocked IS 'Whether the discount code is blocked';

