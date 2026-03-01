DROP INDEX IF EXISTS uq_cfg_currency_tenant_code;

CREATE UNIQUE INDEX IF NOT EXISTS uq_cfg_currency_tenant_code
    ON cfg_currency (tenant_id, code)
    WHERE deleted_at IS NULL;
