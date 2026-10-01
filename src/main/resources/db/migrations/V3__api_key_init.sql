DROP TABLE api_keys;
CREATE TABLE api_keys (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    limit_reset_ms INTEGER NOT NULL DEFAULT 1000
);

CREATE INDEX idx_api_keys_user_id ON api_keys(user_id);