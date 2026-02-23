ALTER TABLE bus_client
    ADD COLUMN IF NOT EXISTS tenant_id UUID,
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE bus_client
SET tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL;

ALTER TABLE bus_client
    ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE bus_provider
    ADD COLUMN IF NOT EXISTS tenant_id UUID,
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE bus_provider
SET tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL;

ALTER TABLE bus_provider
    ALTER COLUMN tenant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_bus_client_tenant_active ON bus_client (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_bus_provider_tenant_active ON bus_provider (tenant_id, active);
