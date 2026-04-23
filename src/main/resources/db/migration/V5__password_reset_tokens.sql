CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    token VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_password_reset_tokens_token UNIQUE (token)
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
