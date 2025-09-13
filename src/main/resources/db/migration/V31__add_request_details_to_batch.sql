-- Add request details columns to batch table
ALTER TABLE batch ADD COLUMN gift_card_requests TEXT;
ALTER TABLE batch ADD COLUMN discount_code_requests TEXT;
