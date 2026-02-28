-- 1) Agregar columna
ALTER TABLE auth_role_module
ADD COLUMN tenant_id uuid;

-- 2) Backfill: inferir tenant_id desde role (si role tiene tenant_id)
UPDATE auth_role_module arm
SET tenant_id = r.tenant_id
FROM auth_role r
WHERE arm.role_id = r.id
  AND arm.tenant_id IS NULL;

-- 3) Validación: si aún hay nulls, es data inconsistente (mejor fallar)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM auth_role_module WHERE tenant_id IS NULL) THEN
    RAISE EXCEPTION 'auth_role_module.tenant_id has NULL values after backfill';
  END IF;
END $$;

-- 4) Ya con data OK, hacerlo NOT NULL
ALTER TABLE auth_role_module
ALTER COLUMN tenant_id SET NOT NULL;

-- 5) (Opcional pero recomendado) FK al tenant
-- Si tu tabla se llama tenant:
ALTER TABLE auth_role_module
ADD CONSTRAINT fk_auth_role_module_tenant
FOREIGN KEY (tenant_id) REFERENCES tenant(id);

-- 6) Índice para queries por tenant
CREATE INDEX IF NOT EXISTS idx_auth_role_module_tenant
ON auth_role_module(tenant_id);

-- 7) (Opcional recomendado) evitar duplicados por tenant
-- Si el PK no lo cubre ya:
CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_role_module_tenant_role_module
ON auth_role_module(tenant_id, role_id, module_id);