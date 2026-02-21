ALTER TABLE module_license
    ADD COLUMN IF NOT EXISTS start_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS end_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_module_license_tenant_window
    ON module_license (tenant_id, start_at, end_at);
