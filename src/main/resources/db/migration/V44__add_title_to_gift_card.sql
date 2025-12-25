-- Add title column to gift_card table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'gift_card' AND column_name = 'title'
    ) THEN
        ALTER TABLE gift_card ADD COLUMN title VARCHAR(255) NOT NULL DEFAULT '';
    END IF;
END $$;

-- Update all existing gift cards to have empty string title if NULL
UPDATE gift_card SET title = '' WHERE title IS NULL;

-- Add comment to explain the column
COMMENT ON COLUMN gift_card.title IS 'Title/description for the gift card';
