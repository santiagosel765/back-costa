ALTER TABLE auth_role_module
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

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

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM auth_role_module WHERE tenant_id IS NULL) THEN
        RAISE EXCEPTION 'auth_role_module.tenant_id has NULL values after backfill';
    END IF;
END
$$;

ALTER TABLE auth_role_module
    ALTER COLUMN tenant_id SET NOT NULL;

DO $$
BEGIN
    IF to_regclass('tenant') IS NOT NULL
        AND NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_auth_role_module_tenant'
        ) THEN
        ALTER TABLE auth_role_module
            ADD CONSTRAINT fk_auth_role_module_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenant (id)
                    ON UPDATE RESTRICT ON DELETE RESTRICT;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_auth_role_module_tenant_id
    ON auth_role_module (tenant_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_role_module_tenant_role_module
    ON auth_role_module (tenant_id, auth_role_id, auth_module_id);
