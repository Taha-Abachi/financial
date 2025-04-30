ALTER TABLE gift_card_transaction
    ADD COLUMN "balance_before" int8 not null DEFAULT 0;