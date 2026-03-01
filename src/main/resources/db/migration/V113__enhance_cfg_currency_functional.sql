ALTER TABLE cfg_currency
    ADD COLUMN IF NOT EXISTS symbol VARCHAR(10),
    ADD COLUMN IF NOT EXISTS decimals INT NOT NULL DEFAULT 2,
    ADD COLUMN IF NOT EXISTS is_functional BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS exchange_rate_ref DOUBLE PRECISION;

UPDATE cfg_currency
SET code = UPPER(TRIM(code))
WHERE code IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_cfg_currency_tenant_functional
    ON cfg_currency (tenant_id)
    WHERE is_functional = TRUE AND deleted_at IS NULL;
