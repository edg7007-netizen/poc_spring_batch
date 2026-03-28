CREATE TABLE IF NOT EXISTS phone_feature_store
(
    id                               BIGSERIAL PRIMARY KEY,
    phone_number                     VARCHAR(20)    NOT NULL,
    user_id                          VARCHAR(50),
    score_date                       DATE           NOT NULL,

    -- Raw features
    previous_loans_count             INTEGER        NOT NULL DEFAULT 0,
    current_balance                  NUMERIC(15, 2),
    app_installed                    BOOLEAN        NOT NULL DEFAULT FALSE,
    ptp_made_count                   INTEGER        NOT NULL DEFAULT 0,
    has_broken_ptp                   BOOLEAN        NOT NULL DEFAULT FALSE,
    days_since_last_contact          BIGINT         NOT NULL DEFAULT 0,
    historical_answer_rate_same_hour DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    historical_answer_rate_same_dow  DOUBLE PRECISION NOT NULL DEFAULT 0.0,

    -- Computed scores
    reach_score                      DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    recovery_score                   DOUBLE PRECISION NOT NULL DEFAULT 0.0,

    -- Audit
    computed_at                      TIMESTAMP,
    created_at                       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pfs_phone_date ON phone_feature_store (phone_number, score_date);
CREATE INDEX IF NOT EXISTS idx_pfs_user_date  ON phone_feature_store (user_id, score_date);
CREATE INDEX IF NOT EXISTS idx_pfs_score_date ON phone_feature_store (score_date, reach_score DESC);
