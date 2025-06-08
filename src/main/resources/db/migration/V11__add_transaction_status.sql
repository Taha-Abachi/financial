ALTER TABLE gift_card_transaction
    ADD COLUMN status VARCHAR(20);

UPDATE gift_card_transaction
SET status = 'Pending'
WHERE transaction_type = 'Debit' AND status IS NULL;
