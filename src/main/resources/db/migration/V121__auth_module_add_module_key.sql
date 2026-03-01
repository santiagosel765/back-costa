-- Introduce stable technical key for modules and enforce active uniqueness per tenant.

ALTER TABLE auth_module
    ADD COLUMN IF NOT EXISTS module_key VARCHAR(100);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_proc
        WHERE proname = 'normalize_module_key'
          AND pg_function_is_visible(oid)
          AND pg_get_function_identity_arguments(oid) = 'module_name text'
    ) THEN
        EXECUTE $fn$
            CREATE FUNCTION normalize_module_key(module_name TEXT)
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
            $$
        $fn$;
    END IF;
END
$$;

-- Generic backfill from current visible label.
UPDATE auth_module
SET module_key = normalize_module_key(name)
WHERE module_key IS NULL OR BTRIM(module_key) = '';

-- Canonical key aliases to keep a single technical identifier across languages/legacy keys.
UPDATE auth_module
SET module_key = 'CONFIG'
WHERE normalize_module_key(name) IN ('CONFIG', 'CONFIGURACION', 'SETTINGS')
   OR normalize_module_key(module_key) IN ('CONFIG', 'CONFIGURACION', 'SETTINGS');

UPDATE auth_module
SET module_key = 'ORG'
WHERE normalize_module_key(name) IN ('ORG', 'ORGANIZACION', 'ORG_BRANCH', 'SUCURSALES_Y_ORGANIZACIONES')
   OR normalize_module_key(module_key) IN ('ORG', 'ORGANIZACION', 'ORG_BRANCH', 'SUCURSALES_Y_ORGANIZACIONES');

UPDATE auth_module
SET module_key = 'INVENTORY'
WHERE normalize_module_key(name) IN ('INVENTARIO', 'INVENTORY')
   OR normalize_module_key(module_key) IN ('INVENTARIO', 'INVENTORY');

UPDATE auth_module
SET module_key = 'CORE_AUTH'
WHERE normalize_module_key(name) IN ('CORE_DE_AUTENTICACION', 'CORE_AUTH', 'AUTH_CORE')
   OR normalize_module_key(module_key) IN ('CORE_DE_AUTENTICACION', 'CORE_AUTH', 'AUTH_CORE');

UPDATE auth_module
SET module_key = 'AR'
WHERE normalize_module_key(name) IN ('CUENTAS_POR_COBRAR', 'AR')
   OR normalize_module_key(module_key) IN ('CUENTAS_POR_COBRAR', 'AR');

UPDATE auth_module
SET module_key = 'AP'
WHERE normalize_module_key(name) IN ('CUENTAS_POR_PAGAR', 'AP')
   OR normalize_module_key(module_key) IN ('CUENTAS_POR_PAGAR', 'AP');

UPDATE auth_module
SET module_key = 'PURCHASE'
WHERE normalize_module_key(name) IN ('COMPRAS', 'PURCHASE')
   OR normalize_module_key(module_key) IN ('COMPRAS', 'PURCHASE');

UPDATE auth_module
SET module_key = 'SALES'
WHERE normalize_module_key(name) IN ('VENTAS', 'SALES')
   OR normalize_module_key(module_key) IN ('VENTAS', 'SALES');

UPDATE auth_module
SET module_key = 'ACCOUNTING'
WHERE normalize_module_key(name) IN ('CONTABILIDAD', 'ACCOUNTING')
   OR normalize_module_key(module_key) IN ('CONTABILIDAD', 'ACCOUNTING');

UPDATE auth_module
SET module_key = 'BANKS'
WHERE normalize_module_key(name) IN ('BANCOS', 'BANKS')
   OR normalize_module_key(module_key) IN ('BANCOS', 'BANKS');

UPDATE auth_module
SET module_key = 'REPORTING_BI'
WHERE normalize_module_key(name) IN ('REPORTES_Y_BI', 'REPORTING_BI')
   OR normalize_module_key(module_key) IN ('REPORTES_Y_BI', 'REPORTING_BI');

UPDATE auth_module
SET module_key = 'AUDIT_LOGS'
WHERE normalize_module_key(name) IN ('AUDITORIA_Y_LOGS', 'AUDIT_LOGS')
   OR normalize_module_key(module_key) IN ('AUDITORIA_Y_LOGS', 'AUDIT_LOGS');

ALTER TABLE auth_module
    ALTER COLUMN module_key SET NOT NULL;

-- Safety pre-pass so the active unique index can be created without failing.
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY tenant_id, module_key
               ORDER BY updated_at DESC NULLS LAST, created_at DESC NULLS LAST, id
           ) AS row_num
    FROM auth_module
    WHERE status = 1
)
UPDATE auth_module am
SET status = 0
FROM ranked r
WHERE am.id = r.id
  AND r.row_num > 1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_module_tenant_module_key_active
    ON auth_module (tenant_id, module_key)
    WHERE status = 1;
