-- README:
-- Seed de Organización para default-tenant (00000000-0000-0000-0000-000000000001).
-- Crea datos mínimos para UI/endpoint de Organización:
--   1) una sucursal base,
--   2) una bodega principal asociada,
--   3) numeración inicial por tipo de documento activo,
--   4) una asignación mínima usuario-sucursal (si existe usuario en el tenant).
-- Es idempotente: cada INSERT usa claves estables + verificaciones NOT EXISTS,
-- por lo que puede re-ejecutarse sin duplicar ni modificar datos ya existentes del usuario.

WITH default_tenant AS (
    SELECT '00000000-0000-0000-0000-000000000001'::uuid AS tenant_id
)
INSERT INTO org_branch (
    id,
    tenant_id,
    code,
    name,
    description,
    address_line1,
    city,
    state,
    country,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    '20000000-0000-0000-0000-000000000001'::uuid,
    dt.tenant_id,
    'MAIN',
    'Sucursal Principal',
    'Sucursal de prueba para configuración inicial',
    'Zona 10',
    'Guatemala',
    'Guatemala',
    'Guatemala',
    TRUE,
    NOW(),
    NOW(),
    'system',
    'system'
FROM default_tenant dt
WHERE EXISTS (
    SELECT 1
    FROM tenant t
    WHERE t.id = dt.tenant_id
)
  AND NOT EXISTS (
    SELECT 1
    FROM org_branch b
    WHERE b.tenant_id = dt.tenant_id
      AND UPPER(b.code) = 'MAIN'
      AND b.deleted_at IS NULL
  );

WITH default_tenant AS (
    SELECT '00000000-0000-0000-0000-000000000001'::uuid AS tenant_id
), branch_target AS (
    SELECT b.id, b.tenant_id
    FROM org_branch b
    JOIN default_tenant dt ON dt.tenant_id = b.tenant_id
    WHERE UPPER(b.code) = 'MAIN'
      AND b.deleted_at IS NULL
    ORDER BY b.created_at NULLS LAST, b.id
    LIMIT 1
)
INSERT INTO org_warehouse (
    id,
    tenant_id,
    branch_id,
    code,
    name,
    description,
    warehouse_type,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    '20000000-0000-0000-0000-000000000002'::uuid,
    bt.tenant_id,
    bt.id,
    'MAIN',
    'Bodega Principal',
    'Bodega de prueba para configuración inicial',
    'MAIN',
    TRUE,
    NOW(),
    NOW(),
    'system',
    'system'
FROM branch_target bt
WHERE NOT EXISTS (
    SELECT 1
    FROM org_warehouse w
    WHERE w.tenant_id = bt.tenant_id
      AND w.branch_id = bt.id
      AND UPPER(w.code) = 'MAIN'
      AND w.deleted_at IS NULL
);

WITH default_tenant AS (
    SELECT '00000000-0000-0000-0000-000000000001'::uuid AS tenant_id
), branch_target AS (
    SELECT b.id, b.tenant_id
    FROM org_branch b
    JOIN default_tenant dt ON dt.tenant_id = b.tenant_id
    WHERE UPPER(b.code) = 'MAIN'
      AND b.deleted_at IS NULL
    ORDER BY b.created_at NULLS LAST, b.id
    LIMIT 1
)
INSERT INTO org_document_numbering (
    id,
    tenant_id,
    branch_id,
    document_type_id,
    series,
    next_number,
    padding,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    gen_random_uuid(),
    bt.tenant_id,
    bt.id,
    dt.id,
    CASE
        WHEN UPPER(dt.code) = 'INV' THEN 'F001'
        WHEN UPPER(dt.code) = 'BIL' THEN 'B001'
        WHEN UPPER(dt.code) = 'NCR' THEN 'NC01'
        ELSE 'S001'
    END AS series,
    1,
    8,
    TRUE,
    NOW(),
    NOW(),
    'system',
    'system'
FROM branch_target bt
JOIN cfg_document_type dt
  ON dt.tenant_id = bt.tenant_id
 AND dt.active = TRUE
 AND dt.deleted_at IS NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM org_document_numbering dn
    WHERE dn.tenant_id = bt.tenant_id
      AND dn.branch_id = bt.id
      AND dn.document_type_id = dt.id
      AND dn.series = CASE
          WHEN UPPER(dt.code) = 'INV' THEN 'F001'
          WHEN UPPER(dt.code) = 'BIL' THEN 'B001'
          WHEN UPPER(dt.code) = 'NCR' THEN 'NC01'
          ELSE 'S001'
      END
      AND dn.deleted_at IS NULL
);

WITH default_tenant AS (
    SELECT '00000000-0000-0000-0000-000000000001'::uuid AS tenant_id
), branch_target AS (
    SELECT b.id, b.tenant_id
    FROM org_branch b
    JOIN default_tenant dt ON dt.tenant_id = b.tenant_id
    WHERE UPPER(b.code) = 'MAIN'
      AND b.deleted_at IS NULL
    ORDER BY b.created_at NULLS LAST, b.id
    LIMIT 1
), tenant_user AS (
    SELECT u.id AS user_id, u.tenant_id
    FROM auth_user u
    JOIN default_tenant dt ON dt.tenant_id = u.tenant_id
    ORDER BY u.created_at NULLS LAST, u.id
    LIMIT 1
)
INSERT INTO org_user_branch (
    id,
    tenant_id,
    user_id,
    branch_id,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    gen_random_uuid(),
    tu.tenant_id,
    tu.user_id,
    bt.id,
    TRUE,
    NOW(),
    NOW(),
    'system',
    'system'
FROM tenant_user tu
JOIN branch_target bt ON bt.tenant_id = tu.tenant_id
WHERE NOT EXISTS (
    SELECT 1
    FROM org_user_branch ub
    WHERE ub.tenant_id = tu.tenant_id
      AND ub.user_id = tu.user_id
      AND ub.branch_id = bt.id
      AND ub.deleted_at IS NULL
);
