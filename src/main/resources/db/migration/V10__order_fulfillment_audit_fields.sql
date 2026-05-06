ALTER TABLE orders
    ADD COLUMN delivered_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN delivered_by_user_id UUID,
    ADD COLUMN closed_by_user_id UUID;

