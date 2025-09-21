-- Add store_id column to users table for store users
ALTER TABLE users ADD COLUMN store_id BIGINT;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_user_store FOREIGN KEY (store_id) REFERENCES store(id);

-- Add index for store_id
CREATE INDEX idx_user_store ON users(store_id);
