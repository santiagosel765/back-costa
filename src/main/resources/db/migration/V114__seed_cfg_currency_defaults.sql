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
INSERT INTO cfg_currency (id, tenant_id, code, name, description, symbol, decimals, is_functional, active, created_at, updated_at)
SELECT seed.id,
       selected_tenant.id,
       seed.code,
       seed.name,
       seed.description,
       seed.symbol,
       seed.decimals,
       seed.is_functional,
       TRUE,
       NOW(),
       NOW()
FROM selected_tenant
CROSS JOIN (
    VALUES
        ('10000000-0000-0000-0000-000000000001'::UUID, 'GTQ', 'Quetzal', 'Moneda funcional del tenant', 'Q', 2, TRUE),
        ('10000000-0000-0000-0000-000000000002'::UUID, 'USD', 'Dólar', 'Moneda de referencia en dólares', '$', 2, FALSE)
) AS seed (id, code, name, description, symbol, decimals, is_functional)
ON CONFLICT (tenant_id, code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    symbol = EXCLUDED.symbol,
    decimals = EXCLUDED.decimals,
    is_functional = EXCLUDED.is_functional,
    active = TRUE,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW();
