-- Add type column to gift_card table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'gift_card' AND column_name = 'type'
    ) THEN
        ALTER TABLE gift_card ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'PHYSICAL';
    END IF;
END $$;

-- Update all existing gift cards to PHYSICAL type (if any are NULL or empty)
UPDATE gift_card SET type = 'PHYSICAL' WHERE type IS NULL OR type = '';

-- Add comment to explain the column
-- Note: COMMENT ON COLUMN doesn't fail if comment already exists, so it's safe to run multiple times
COMMENT ON COLUMN gift_card.type IS 'Type of gift card: PHYSICAL for physical gift cards, DIGITAL for digital gift cards.';

-- Add check constraint to ensure valid values (if constraint doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_gift_card_type' AND table_name = 'gift_card'
    ) THEN
        ALTER TABLE gift_card ADD CONSTRAINT chk_gift_card_type CHECK (type IN ('PHYSICAL', 'DIGITAL'));
    END IF;
END $$;

