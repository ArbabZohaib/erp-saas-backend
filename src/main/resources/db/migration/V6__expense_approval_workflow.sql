ALTER TABLE expenses
    ADD COLUMN approval_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    ADD COLUMN approval_note VARCHAR(2048),
    ADD COLUMN approved_by_user_id UUID,
    ADD COLUMN approved_at TIMESTAMP WITH TIME ZONE;

