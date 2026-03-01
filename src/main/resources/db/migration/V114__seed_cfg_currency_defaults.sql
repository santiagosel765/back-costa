CREATE EXTENSION IF NOT EXISTS pgcrypto;

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
UPDATE cfg_currency
SET
    is_functional = FALSE,
    updated_at = NOW()
WHERE tenant_id = (SELECT id FROM selected_tenant)
  AND code <> 'GTQ'
  AND is_functional = TRUE
  AND deleted_at IS NULL;

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
UPDATE cfg_currency
SET
    name = 'Quetzal',
    description = 'Moneda funcional del tenant',
    symbol = 'Q',
    decimals = 2,
    is_functional = TRUE,
    active = TRUE,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW()
WHERE tenant_id = (SELECT id FROM selected_tenant)
  AND code = 'GTQ';

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
UPDATE cfg_currency
SET
    name = 'Dólar',
    description = 'Moneda de referencia en dólares',
    symbol = '$',
    decimals = 2,
    is_functional = FALSE,
    active = TRUE,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW()
WHERE tenant_id = (SELECT id FROM selected_tenant)
  AND code = 'USD';

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
INSERT INTO cfg_currency (id, tenant_id, code, name, description, symbol, decimals, is_functional, active, created_at, updated_at)
SELECT
    gen_random_uuid(),
    selected_tenant.id,
    'GTQ',
    'Quetzal',
    'Moneda funcional del tenant',
    'Q',
    2,
    TRUE,
    TRUE,
    NOW(),
    NOW()
FROM selected_tenant
WHERE NOT EXISTS (
    SELECT 1
    FROM cfg_currency
    WHERE tenant_id = selected_tenant.id
      AND code = 'GTQ'
);

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
INSERT INTO cfg_currency (id, tenant_id, code, name, description, symbol, decimals, is_functional, active, created_at, updated_at)
SELECT
    gen_random_uuid(),
    selected_tenant.id,
    'USD',
    'Dólar',
    'Moneda de referencia en dólares',
    '$',
    2,
    FALSE,
    TRUE,
    NOW(),
    NOW()
FROM selected_tenant
WHERE NOT EXISTS (
    SELECT 1
    FROM cfg_currency
    WHERE tenant_id = selected_tenant.id
      AND code = 'USD'
);
