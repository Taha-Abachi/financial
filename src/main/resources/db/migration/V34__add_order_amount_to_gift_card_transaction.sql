-- Add order_amount column to gift_card_transaction table
ALTER TABLE gift_card_transaction ADD COLUMN order_amount BIGINT DEFAULT 0;

-- Add comment to the column
COMMENT ON COLUMN gift_card_transaction.order_amount IS 'The total order amount for which the gift card was used';
