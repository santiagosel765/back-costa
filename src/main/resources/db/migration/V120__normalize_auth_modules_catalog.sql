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
WHERE normalize_module_key(name) IN ('CONFIG', 'CONFIGURACION');

UPDATE auth_module
SET name = 'ORG'
WHERE normalize_module_key(name) IN ('ORG', 'ORGANIZACION', 'SUCURSALES_Y_ORGANIZACIONES', 'ORG_BRANCH');

UPDATE auth_module
SET name = 'INVENTORY'
WHERE normalize_module_key(name) = 'INVENTARIO';

-- Fuerza IDs canónicos históricos de V3 para evitar bifurcaciones por UUID.
UPDATE auth_module
SET name = 'CONFIG'
WHERE id = '8b31ce1f-777c-578c-b219-8712c745f1cf'::uuid;

UPDATE auth_module
SET name = 'ORG'
WHERE id = 'e4738266-3e79-5b32-8a91-9f2f3ee27aa2'::uuid;

-- Garantiza descripciones canónicas para los módulos de hub.
UPDATE auth_module
SET description = 'Configuración y parámetros maestros'
WHERE normalize_module_key(name) = 'CONFIG';

UPDATE auth_module
SET description = 'Gestión organizacional de sucursales y bodegas'
WHERE normalize_module_key(name) = 'ORG';

UPDATE auth_module
SET description = 'Control de existencias, movimientos, lotes y series.'
WHERE normalize_module_key(name) = 'INVENTORY';

-- Construye mapa de duplicados por tenant + key normalizada conservando siempre MIN(id).
CREATE TEMP TABLE duplicates_map (
    dup_id UUID PRIMARY KEY,
    canonical_id UUID NOT NULL,
    tenant_id UUID,
    key_norm TEXT NOT NULL
) ON COMMIT DROP;

INSERT INTO duplicates_map (dup_id, canonical_id, tenant_id, key_norm)
WITH ranked AS (
    SELECT am.id,
           am.tenant_id,
           normalize_module_key(am.name::text) AS key_norm,
           ROW_NUMBER() OVER (
               PARTITION BY am.tenant_id, normalize_module_key(am.name::text)
               ORDER BY am.id
           ) AS rn,
           MIN(am.id) OVER (
               PARTITION BY am.tenant_id, normalize_module_key(am.name::text)
           ) AS canonical_id
    FROM auth_module am
)
SELECT r.id,
       r.canonical_id,
       r.tenant_id,
       r.key_norm
FROM ranked r
WHERE r.rn > 1;

-- Repoint dinámico de cualquier FK simple que apunte a auth_module(id).
DO $$
DECLARE
    fk_rec RECORD;
BEGIN
    FOR fk_rec IN
        SELECT ns.nspname AS schema_name,
               rel.relname AS table_name,
               att.attname AS column_name
        FROM pg_constraint con
        JOIN pg_class rel
          ON rel.oid = con.conrelid
        JOIN pg_namespace ns
          ON ns.oid = rel.relnamespace
        JOIN pg_attribute att
          ON att.attrelid = con.conrelid
         AND att.attnum = con.conkey[1]
        WHERE con.contype = 'f'
          AND con.confrelid = 'auth_module'::regclass
          AND array_length(con.conkey, 1) = 1
          AND array_length(con.confkey, 1) = 1
          AND con.confkey[1] = (
              SELECT attnum
              FROM pg_attribute
              WHERE attrelid = 'auth_module'::regclass
                AND attname = 'id'
                AND NOT attisdropped
              LIMIT 1
          )
    LOOP
        EXECUTE format(
            'UPDATE %I.%I t
             SET %I = d.canonical_id
             FROM duplicates_map d
             WHERE t.%I = d.dup_id',
            fk_rec.schema_name,
            fk_rec.table_name,
            fk_rec.column_name,
            fk_rec.column_name
        );
    END LOOP;
END
$$;

-- Borra definitivamente los módulos duplicados ya repointados.
DELETE FROM auth_module am
USING duplicates_map d
WHERE am.id = d.dup_id;

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
),
seed_modules AS (
    SELECT modules.id,
           modules.name,
           modules.description,
           modules.status,
           selected_tenant.id AS tenant_id,
           normalize_module_key(modules.name) AS key_norm
    FROM selected_tenant
    CROSS JOIN (
        VALUES
            ('8b31ce1f-777c-578c-b219-8712c745f1cf'::UUID, 'CONFIG', 'Configuración y parámetros maestros', 1),
            ('e4738266-3e79-5b32-8a91-9f2f3ee27aa2'::UUID, 'ORG', 'Gestión organizacional de sucursales y bodegas', 1)
    ) AS modules (id, name, description, status)
)
INSERT INTO auth_module (id, name, description, status, tenant_id)
SELECT sm.id,
       sm.name,
       sm.description,
       sm.status,
       sm.tenant_id
FROM seed_modules sm
WHERE NOT EXISTS (
    SELECT 1
    FROM auth_module am
    WHERE am.tenant_id = sm.tenant_id
      AND normalize_module_key(am.name) = sm.key_norm
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    tenant_id = EXCLUDED.tenant_id;

DROP INDEX IF EXISTS uq_auth_module_name;
DROP INDEX IF EXISTS uq_auth_module_tenant_key_ci;
CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_module_tenant_key_ci
    ON auth_module (tenant_id, normalize_module_key(name));
