-- Add isActive column to store table for logical delete functionality
ALTER TABLE store
    ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- Add index for better performance on isActive queries
CREATE INDEX idx_store_is_active ON store(is_active);

-- Add comment for documentation
COMMENT ON COLUMN store.is_active IS 'Flag to indicate if the store is active (true) or logically deleted (false)';
