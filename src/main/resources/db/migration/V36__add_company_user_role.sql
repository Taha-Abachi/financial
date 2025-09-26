-- Add COMPANY_USER role to existing user_role table
INSERT INTO user_role (name, description) VALUES
('COMPANY_USER', 'Company User with access to company-specific operations and data')
ON CONFLICT (name) DO NOTHING;
