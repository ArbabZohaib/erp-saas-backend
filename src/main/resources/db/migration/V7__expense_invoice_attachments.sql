CREATE TABLE expense_invoice_attachments (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    expense_id UUID NOT NULL REFERENCES expenses (id) ON DELETE CASCADE,
    file_name VARCHAR(512) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_expense_invoice_attachments_org_expense
    ON expense_invoice_attachments (org_id, expense_id);
