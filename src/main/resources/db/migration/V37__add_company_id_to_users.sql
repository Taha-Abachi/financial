-- Add company_id column to users table for COMPANY_USER role
ALTER TABLE users
    ADD COLUMN company_id BIGINT;

-- Add foreign key constraint
ALTER TABLE users
    ADD CONSTRAINT fk_users_company_id
    FOREIGN KEY (company_id) REFERENCES company(id);

-- Add index for better performance
CREATE INDEX idx_users_company_id ON users(company_id);

-- Add comment
COMMENT ON COLUMN users.company_id IS 'Company ID for COMPANY_USER role - references company table';
