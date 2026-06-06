CREATE TABLE __TABLE_PREFIX___nonce
(
    nonce      VARCHAR(255) PRIMARY KEY,
    not_before TIMESTAMPTZ NOT NULL,
    not_after  TIMESTAMPTZ NOT NULL,
    used       BOOLEAN     NOT NULL DEFAULT FALSE,
    used_at    TIMESTAMPTZ
);

CREATE INDEX idx___TABLE_PREFIX___nonce_validity
    ON __TABLE_PREFIX___nonce (used, not_before, not_after);

CREATE INDEX idx___TABLE_PREFIX___nonce_cleanup
    ON __TABLE_PREFIX___nonce (not_after, used);

-- Optional: faster cleanup scans
CREATE INDEX idx___TABLE_PREFIX___nonce_expired
    ON __TABLE_PREFIX___nonce (not_after)
    WHERE used = FALSE;
