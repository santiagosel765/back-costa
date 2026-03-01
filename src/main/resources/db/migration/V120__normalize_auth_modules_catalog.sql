-- Normaliza el catálogo de módulos para evitar duplicidades por alias.

CREATE OR REPLACE FUNCTION normalize_module_key(module_name TEXT)
RETURNS TEXT
LANGUAGE SQL
IMMUTABLE
AS $$
    SELECT UPPER(
        REGEXP_REPLACE(
            TRANSLATE(COALESCE(module_name, ''),
                'áéíóúÁÉÍÓÚäëïöüÄËÏÖÜñÑ',
                'aeiouAEIOUaeiouAEIOUnN'),
            '[^A-Za-z0-9]+',
            '_',
            'g'))
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'auth_module'::regclass
          AND conname = 'auth_module_name_key'
    ) THEN
        ALTER TABLE auth_module DROP CONSTRAINT auth_module_name_key;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'auth_module'::regclass
          AND conname = 'uq_auth_module_name'
    ) THEN
        ALTER TABLE auth_module DROP CONSTRAINT uq_auth_module_name;
    END IF;
END
$$;

-- Aliases -> key canónica.
UPDATE auth_module
SET name = 'CONFIG'
WHERE normalize_module_key(name) IN ('CONFIG', 'CONFIGURACION', 'SETTINGS');

UPDATE auth_module
SET name = 'ORG'
WHERE normalize_module_key(name) IN ('ORG', 'ORGANIZACION', 'SUCURSALES_Y_ORGANIZACIONES', 'ORG_BRANCH');

UPDATE auth_module
SET name = 'INVENTORY'
WHERE normalize_module_key(name) = 'INVENTARIO';

-- Garantiza descripciones canónicas para los módulos de hub.
UPDATE auth_module
SET description = 'Configuración y parámetros maestros'
WHERE name = 'CONFIG';

UPDATE auth_module
SET description = 'Gestión organizacional de sucursales y bodegas'
WHERE name = 'ORG';

UPDATE auth_module
SET description = 'Control de existencias, movimientos, lotes y series.'
WHERE name = 'INVENTORY';

-- Si quedaron registros activos duplicados para la misma key canónica por tenant,
-- mantiene uno activo y desactiva el resto.
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY tenant_id, normalize_module_key(name)
               ORDER BY
                   CASE WHEN name = normalize_module_key(name) THEN 0 ELSE 1 END,
                   updated_at DESC NULLS LAST,
                   created_at DESC NULLS LAST,
                   id
           ) AS row_num
    FROM auth_module
    WHERE status = 1
)
UPDATE auth_module am
SET status = 0
FROM ranked r
WHERE am.id = r.id
  AND r.row_num > 1;

-- Reasigna auth_role_module al id canónico activo evitando colisiones por unique duro.
WITH canonical AS (
    SELECT DISTINCT ON (tenant_id, normalize_module_key(name))
           id,
           tenant_id,
           normalize_module_key(name) AS canonical_key
    FROM auth_module
    WHERE status = 1
    ORDER BY tenant_id, normalize_module_key(name),
             CASE WHEN name = normalize_module_key(name) THEN 0 ELSE 1 END,
             updated_at DESC NULLS LAST,
             created_at DESC NULLS LAST,
             id
),
arm_targets AS (
    SELECT arm.id,
           arm.tenant_id,
           arm.auth_role_id,
           arm.auth_module_id,
           c.id AS canonical_module_id,
           ROW_NUMBER() OVER (
               PARTITION BY arm.tenant_id, arm.auth_role_id, c.id
               ORDER BY
                   CASE WHEN arm.status = 1 THEN 0 ELSE 1 END,
                   arm.updated_at DESC NULLS LAST,
                   arm.created_at DESC NULLS LAST,
                   arm.id
           ) AS row_num
    FROM auth_role_module arm
    JOIN auth_module am
      ON am.id = arm.auth_module_id
    JOIN canonical c
      ON c.tenant_id = am.tenant_id
     AND c.canonical_key = normalize_module_key(am.name)
)
DELETE FROM auth_role_module arm
USING arm_targets t
WHERE arm.id = t.id
  AND t.row_num > 1;

WITH canonical AS (
    SELECT DISTINCT ON (tenant_id, normalize_module_key(name))
           id,
           tenant_id,
           normalize_module_key(name) AS canonical_key
    FROM auth_module
    WHERE status = 1
    ORDER BY tenant_id, normalize_module_key(name),
             CASE WHEN name = normalize_module_key(name) THEN 0 ELSE 1 END,
             updated_at DESC NULLS LAST,
             created_at DESC NULLS LAST,
             id
)
UPDATE auth_role_module arm
SET auth_module_id = c.id
FROM auth_module am
JOIN canonical c
  ON c.tenant_id = am.tenant_id
 AND c.canonical_key = normalize_module_key(am.name)
WHERE arm.auth_module_id = am.id
  AND arm.auth_module_id <> c.id;

-- Mantiene la semilla CONFIG/ORG idempotente con las claves canónicas.
WITH selected_tenant AS (
    SELECT id
    FROM tenant
    ORDER BY
        CASE
            WHEN LOWER(name) IN ('default', 'default-tenant') THEN 0
            ELSE 1
        END,
        created_at NULLS LAST,
        id
    LIMIT 1
)
INSERT INTO auth_module (id, name, description, status, tenant_id)
SELECT modules.id,
       modules.name,
       modules.description,
       modules.status,
       selected_tenant.id
FROM selected_tenant
CROSS JOIN (
    VALUES
        ('1f4f06e2-4438-4f48-b16d-26cf27f1064f'::UUID, 'CONFIG', 'Configuración y parámetros maestros', 1),
        ('9dbf5cf0-4937-4f30-a8e8-0e67ba2098dd'::UUID, 'ORG', 'Gestión organizacional de sucursales y bodegas', 1)
) AS modules (id, name, description, status)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    tenant_id = EXCLUDED.tenant_id;

DROP INDEX IF EXISTS uq_auth_module_name;
DROP INDEX IF EXISTS uq_auth_module_tenant_key_ci;
CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_module_tenant_key_ci
    ON auth_module (tenant_id, normalize_module_key(name))
    WHERE status = 1;
