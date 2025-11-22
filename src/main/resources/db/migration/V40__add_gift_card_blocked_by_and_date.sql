-- Add blocked_by_user_id column to gift_card table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'gift_card' AND column_name = 'blocked_by_user_id'
    ) THEN
        ALTER TABLE gift_card ADD COLUMN blocked_by_user_id BIGINT;
        ALTER TABLE gift_card ADD CONSTRAINT fk_gift_card_blocked_by_user 
            FOREIGN KEY (blocked_by_user_id) REFERENCES users(id);
    END IF;
END $$;

-- Add blocked_date column to gift_card table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'gift_card' AND column_name = 'blocked_date'
    ) THEN
        ALTER TABLE gift_card ADD COLUMN blocked_date TIMESTAMP;
    END IF;
END $$;

-- Add comment to explain the columns
COMMENT ON COLUMN gift_card.blocked_by_user_id IS 'User who blocked this gift card';
COMMENT ON COLUMN gift_card.blocked_date IS 'Date and time when the gift card was blocked';

