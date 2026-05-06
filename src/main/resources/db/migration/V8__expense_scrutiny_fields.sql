ALTER TABLE expenses
    ADD COLUMN bill_extracted_amount NUMERIC(19, 4),
    ADD COLUMN bill_extraction_confidence VARCHAR(32),
    ADD COLUMN scrutiny_level VARCHAR(32) NOT NULL DEFAULT 'NO_BILL';

