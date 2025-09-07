-- Create batch table
CREATE TABLE batch (
    id BIGSERIAL PRIMARY KEY,
    batch_number VARCHAR(20) NOT NULL UNIQUE,
    batch_type VARCHAR(20) NOT NULL,
    description TEXT,
    request_date TIMESTAMP NOT NULL,
    total_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_count INTEGER NOT NULL DEFAULT 0,
    failed_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_user_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    CONSTRAINT fk_batch_user FOREIGN KEY (request_user_id) REFERENCES users(id),
    CONSTRAINT fk_batch_company FOREIGN KEY (company_id) REFERENCES company(id)
);

-- Create indexes for better performance
CREATE INDEX idx_batch_number ON batch(batch_number);
CREATE INDEX idx_batch_type ON batch(batch_type);
CREATE INDEX idx_batch_status ON batch(status);
CREATE INDEX idx_batch_request_user ON batch(request_user_id);
CREATE INDEX idx_batch_company ON batch(company_id);
CREATE INDEX idx_batch_request_date ON batch(request_date); 