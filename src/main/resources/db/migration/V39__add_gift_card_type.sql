-- Add type column to gift_card table
ALTER TABLE gift_card ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'PHYSICAL';

-- Update all existing gift cards to PHYSICAL type
UPDATE gift_card SET type = 'PHYSICAL' WHERE type IS NULL OR type = '';

-- Add comment to explain the column
COMMENT ON COLUMN gift_card.type IS 'Type of gift card: PHYSICAL for physical gift cards, DIGITAL for digital gift cards.';

-- Add check constraint to ensure valid values
ALTER TABLE gift_card ADD CONSTRAINT chk_gift_card_type CHECK (type IN ('PHYSICAL', 'DIGITAL'));

