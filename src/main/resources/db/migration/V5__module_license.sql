CREATE TABLE IF NOT EXISTS module_license (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    module_id UUID NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_module_license_tenant_module
    ON module_license (tenant_id, module_id);

CREATE OR REPLACE FUNCTION set_module_license_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_module_license_set_updated_at ON module_license;

CREATE TRIGGER trg_module_license_set_updated_at
BEFORE UPDATE ON module_license
FOR EACH ROW
EXECUTE FUNCTION set_module_license_updated_at();
