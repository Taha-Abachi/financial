ALTER TABLE discount_code_transaction
    ADD COLUMN status VARCHAR(20);

UPDATE discount_code_transaction
SET status = 'Pending'
WHERE trx_type = 'Redeem' AND status IS NULL; 