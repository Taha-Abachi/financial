-- Add company_id column to discount_code table
ALTER TABLE discount_code ADD COLUMN company_id BIGINT;

-- Add foreign key constraint
ALTER TABLE discount_code ADD CONSTRAINT fk_discountcode_company 
    FOREIGN KEY (company_id) REFERENCES company(id);

-- Add index for better performance
CREATE INDEX idx_discountcode_company ON discount_code(company_id); 