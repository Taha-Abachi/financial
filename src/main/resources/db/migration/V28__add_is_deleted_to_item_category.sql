-- Add logical delete and audit fields to item_category table
ALTER TABLE item_category ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE item_category ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE item_category ADD COLUMN delete_user VARCHAR(255);
ALTER TABLE item_category ADD COLUMN deactive_date TIMESTAMP;
ALTER TABLE item_category ADD COLUMN delete_date TIMESTAMP;

-- Update existing records to have proper default values
UPDATE item_category SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE item_category SET is_active = TRUE WHERE is_active IS NULL;
