-- Add API key column to users table (idempotent for PostgreSQL)
DO $$
BEGIN
    -- Check if api_key column doesn't exist before adding it
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'api_key'
    ) THEN
        ALTER TABLE users ADD COLUMN api_key TEXT;
    END IF;
    
    -- Check if index doesn't exist before creating it
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE indexname = 'idx_user_api_key'
    ) THEN
        CREATE INDEX idx_user_api_key ON users(api_key);
    END IF;
    
    -- Update existing users to have empty API key (only if column was just added)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'api_key'
    ) THEN
        UPDATE users SET api_key = NULL WHERE api_key IS NULL;
    END IF;
END $$;
