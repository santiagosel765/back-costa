ALTER TABLE auth_role_module
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

DO $$
DECLARE
    v_default_tenant UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    IF to_regclass('tenant') IS NOT NULL THEN
        INSERT INTO tenant (id, name, status)
        VALUES (v_default_tenant, 'default-tenant', 1)
        ON CONFLICT (id) DO NOTHING;
    END IF;
END
$$;

UPDATE auth_role_module arm
SET tenant_id = ar.tenant_id
FROM auth_role ar
WHERE arm.auth_role_id = ar.id
  AND arm.tenant_id IS NULL;

UPDATE auth_role_module arm
SET tenant_id = am.tenant_id
FROM auth_module am
WHERE arm.auth_module_id = am.id
  AND arm.tenant_id IS NULL;

UPDATE auth_role_module
SET tenant_id = '00000000-0000-0000-0000-000000000001'::uuid
WHERE tenant_id IS NULL;

ALTER TABLE auth_role_module
    ALTER COLUMN tenant_id SET NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_auth_role_module'
          AND conrelid = 'auth_role_module'::regclass
    ) THEN
        ALTER TABLE auth_role_module DROP CONSTRAINT uq_auth_role_module;
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_role_module_tenant_role_module
    ON auth_role_module (tenant_id, auth_role_id, auth_module_id);

CREATE INDEX IF NOT EXISTS idx_auth_role_module_tenant_role
    ON auth_role_module (tenant_id, auth_role_id);

CREATE INDEX IF NOT EXISTS idx_auth_role_module_tenant_module
    ON auth_role_module (tenant_id, auth_module_id);
