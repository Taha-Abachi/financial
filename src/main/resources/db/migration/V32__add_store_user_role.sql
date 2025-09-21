-- Add STORE_USER role to existing user_role table
INSERT INTO user_role (name, description) VALUES
('STORE_USER', 'Store User with access to store-specific transactions')
ON CONFLICT (name) DO NOTHING;
