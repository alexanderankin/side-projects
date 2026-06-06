create table if not exists __TABLE_PREFIX___nonce
(
    nonce      VARCHAR(255) NOT NULL,
    not_before TIMESTAMP(6) NOT NULL,
    not_after  TIMESTAMP(6) NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    used_at    TIMESTAMP(6) NULL,

    PRIMARY KEY (nonce),

    INDEX idx_acme_nonce_validity (used, not_before, not_after),
    INDEX idx_acme_nonce_cleanup (not_after, used)
);
