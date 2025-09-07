-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    login_ip VARCHAR(45),
    user_agent TEXT,
    referrer TEXT,
    is_revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    revoke_reason VARCHAR(255),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_refresh_token_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_token_revoked ON refresh_tokens(is_revoked);
