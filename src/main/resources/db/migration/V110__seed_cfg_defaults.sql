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
SELECT seed.id, selected_tenant.id, seed.code, seed.name, seed.description, seed.symbol, seed.decimals, seed.is_functional, TRUE, NOW(), NOW()
FROM selected_tenant
CROSS JOIN (
    VALUES
        ('10000000-0000-0000-0000-000000000001'::UUID, 'GTQ', 'Quetzal Guatemalteco', 'Moneda funcional del tenant', 'Q', 2, TRUE),
        ('10000000-0000-0000-0000-000000000002'::UUID, 'USD', 'US Dollar', 'Moneda de referencia en dólares', '$', 2, FALSE)
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
INSERT INTO cfg_tax (id, tenant_id, code, name, description, rate, active, created_at, updated_at)
SELECT seed.id, selected_tenant.id, seed.code, seed.name, seed.description, seed.rate, TRUE, NOW(), NOW()
FROM selected_tenant
CROSS JOIN (
    VALUES
        ('10000000-0000-0000-0000-000000000011'::UUID, 'IGV18', 'IGV 18%', 'Impuesto general a las ventas', 0.1800::NUMERIC),
        ('10000000-0000-0000-0000-000000000012'::UUID, 'EXO', 'Exonerado', 'Operación exonerada', 0.0000::NUMERIC),
        ('10000000-0000-0000-0000-000000000013'::UUID, 'INA', 'Inafecto', 'Operación inafecta', 0.0000::NUMERIC)
) AS seed (id, code, name, description, rate)
ON CONFLICT (tenant_id, code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    rate = EXCLUDED.rate,
    active = TRUE,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW();

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
INSERT INTO cfg_parameter (id, tenant_id, code, name, description, value, active, created_at, updated_at)
SELECT seed.id, selected_tenant.id, seed.code, seed.name, seed.description, seed.value, TRUE, NOW(), NOW()
FROM selected_tenant
CROSS JOIN (
    VALUES
        ('10000000-0000-0000-0000-000000000021'::UUID, 'COMPANY_NAME', 'Razón social', 'Nombre legal de la empresa', 'Empresa Demo SAC'),
        ('10000000-0000-0000-0000-000000000022'::UUID, 'COMPANY_RUC', 'RUC', 'Número de documento fiscal', '20123456789'),
        ('10000000-0000-0000-0000-000000000023'::UUID, 'COUNTRY', 'País', 'País de operación', 'PE')
) AS seed (id, code, name, description, value)
ON CONFLICT (tenant_id, code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    value = EXCLUDED.value,
    active = TRUE,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW();

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
INSERT INTO cfg_payment_method (id, tenant_id, code, name, description, active, created_at, updated_at)
SELECT seed.id, selected_tenant.id, seed.code, seed.name, seed.description, TRUE, NOW(), NOW()
FROM selected_tenant
CROSS JOIN (
    VALUES
        ('10000000-0000-0000-0000-000000000031'::UUID, 'CASH', 'Efectivo', 'Pago en efectivo'),
        ('10000000-0000-0000-0000-000000000032'::UUID, 'CARD', 'Tarjeta', 'Pago con tarjeta de débito/crédito'),
        ('10000000-0000-0000-0000-000000000033'::UUID, 'TRANSFER', 'Transferencia', 'Transferencia bancaria')
) AS seed (id, code, name, description)
ON CONFLICT (tenant_id, code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = TRUE,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW();

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
INSERT INTO cfg_document_type (id, tenant_id, code, name, description, active, created_at, updated_at)
SELECT seed.id, selected_tenant.id, seed.code, seed.name, seed.description, TRUE, NOW(), NOW()
FROM selected_tenant
CROSS JOIN (
    VALUES
        ('10000000-0000-0000-0000-000000000041'::UUID, 'INV', 'Factura', 'Documento para operaciones con RUC'),
        ('10000000-0000-0000-0000-000000000042'::UUID, 'BIL', 'Boleta', 'Documento para consumidor final'),
        ('10000000-0000-0000-0000-000000000043'::UUID, 'NCR', 'Nota de crédito', 'Documento de ajuste')
) AS seed (id, code, name, description)
ON CONFLICT (tenant_id, code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = TRUE,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW();
