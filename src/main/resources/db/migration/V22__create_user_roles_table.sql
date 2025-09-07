-- Create user_roles table
CREATE TABLE user_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Insert default roles
INSERT INTO user_role (name, description) VALUES
('SUPERADMIN', 'Super Administrator with full system access'),
('ADMIN', 'Administrator with management access'),
('API_USER', 'API User with limited access'),
('USER', 'Regular user with basic access'); 