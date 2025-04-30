ALTER TABLE gift_card
ALTER COLUMN last_used TYPE timestamp
USING last_used::timestamp;