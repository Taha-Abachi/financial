-- Add company_id column to gift_card table
ALTER TABLE gift_card ADD COLUMN company_id BIGINT;

-- Add foreign key constraint
ALTER TABLE gift_card ADD CONSTRAINT fk_giftcard_company
    FOREIGN KEY (company_id) REFERENCES company(id);

-- Add index for better performance
CREATE INDEX idx_giftcard_company ON gift_card(company_id);
