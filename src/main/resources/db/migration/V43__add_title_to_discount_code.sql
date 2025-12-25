-- Add title column to discount_code table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'discount_code' AND column_name = 'title'
    ) THEN
        ALTER TABLE discount_code ADD COLUMN title VARCHAR(255) NOT NULL DEFAULT '';
    END IF;
END $$;

-- Update all existing discount codes to have empty string title if NULL
UPDATE discount_code SET title = '' WHERE title IS NULL;

-- Add comment to explain the column
COMMENT ON COLUMN discount_code.title IS 'Title/description for the discount code';
