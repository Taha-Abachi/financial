-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    mobile_phone_number VARCHAR(15) NOT NULL UNIQUE,
    national_code VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES user_role(id)
);

-- Create indexes for better performance
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_mobile ON users(mobile_phone_number);
CREATE INDEX idx_user_national_code ON users(national_code);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_role_id ON users(role_id);
CREATE INDEX idx_user_active ON users(is_active); 