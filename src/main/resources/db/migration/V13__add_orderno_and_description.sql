ALTER TABLE gift_card_transaction
    ADD COLUMN orderno VARCHAR(25),
    ADD COLUMN description VARCHAR(250);

ALTER TABLE discount_code_transaction
    ADD COLUMN orderno VARCHAR(25),
    ADD COLUMN description VARCHAR(250); 