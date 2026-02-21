CREATE TABLE IF NOT EXISTS tenant (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

DO $$
DECLARE
    v_default_tenant UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    INSERT INTO tenant (id, name, status)
    VALUES (v_default_tenant, 'default-tenant', 1)
    ON CONFLICT (id) DO NOTHING;
END $$;

ALTER TABLE auth_user
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

UPDATE auth_user
SET tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL;

ALTER TABLE auth_user
    ALTER COLUMN tenant_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_auth_user_tenant'
          AND table_name = 'auth_user'
    ) THEN
        ALTER TABLE auth_user
            ADD CONSTRAINT fk_auth_user_tenant
            FOREIGN KEY (tenant_id) REFERENCES tenant (id)
            ON UPDATE RESTRICT ON DELETE RESTRICT;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_auth_user_tenant_id ON auth_user (tenant_id);
