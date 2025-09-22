-- Add ownership_type and location_type columns to store table
ALTER TABLE store 
ADD COLUMN ownership_type VARCHAR(20),
ADD COLUMN location_type VARCHAR(20);

-- Add comments to explain the column purposes
COMMENT ON COLUMN store.ownership_type IS 'Type of store ownership: OWNED, FRANCHISE, THIRD_PARTY';
COMMENT ON COLUMN store.location_type IS 'Type of store location: ONLINE, FIELD, MARKETPLACE';
