-- JWT Token tracking for invalidation and blacklisting
CREATE TABLE jwt_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(20) NOT NULL CHECK (token_type IN ('ACCESS', 'REFRESH')),
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMPTZ,
    revoked_reason VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_jwt_tokens_user_id ON jwt_tokens(user_id);
CREATE INDEX idx_jwt_tokens_token_hash ON jwt_tokens(token_hash);
CREATE INDEX idx_jwt_tokens_expires_at ON jwt_tokens(expires_at);
CREATE INDEX idx_jwt_tokens_is_revoked ON jwt_tokens(is_revoked);
CREATE INDEX idx_jwt_tokens_token_type ON jwt_tokens(token_type);

-- Composite index for active tokens
CREATE INDEX idx_jwt_tokens_active ON jwt_tokens(user_id, token_type, is_revoked, expires_at);

-- Function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_jwt_tokens_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to automatically update the updated_at field
CREATE TRIGGER trigger_jwt_tokens_updated_at
    BEFORE UPDATE ON jwt_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_jwt_tokens_updated_at();
