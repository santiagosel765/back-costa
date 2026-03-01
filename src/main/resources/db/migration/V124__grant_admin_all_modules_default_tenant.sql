-- Ensure ADMIN role in default tenant has all active modules assigned (idempotent)

WITH target_admin_role AS (
    SELECT ar.id AS role_id, ar.tenant_id
    FROM auth_role ar
    WHERE ar.name = 'ADMIN'
      AND ar.tenant_id = '00000000-0000-0000-0000-000000000001'::uuid
      AND ar.status = 1
    LIMIT 1
),
active_modules AS (
    SELECT am.id AS module_id, am.tenant_id
    FROM auth_module am
    WHERE am.status = 1
      AND am.tenant_id = '00000000-0000-0000-0000-000000000001'::uuid
),
existing_links AS (
    SELECT arm.auth_module_id AS module_id
    FROM auth_role_module arm
    JOIN target_admin_role tar
      ON tar.role_id = arm.auth_role_id
     AND tar.tenant_id = arm.tenant_id
)
INSERT INTO auth_role_module (
    id,
    auth_role_id,
    auth_module_id,
    status,
    tenant_id,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    tar.role_id,
    am.module_id,
    1,
    tar.tenant_id,
    NOW(),
    NOW()
FROM target_admin_role tar
JOIN active_modules am
  ON am.tenant_id = tar.tenant_id
LEFT JOIN existing_links el
  ON el.module_id = am.module_id
WHERE el.module_id IS NULL;

UPDATE auth_role_module arm
SET status = 1,
    updated_at = NOW()
FROM auth_role ar
JOIN auth_module am
  ON am.tenant_id = ar.tenant_id
WHERE arm.auth_role_id = ar.id
  AND am.id = arm.auth_module_id
  AND arm.tenant_id = ar.tenant_id
  AND ar.name = 'ADMIN'
  AND ar.tenant_id = '00000000-0000-0000-0000-000000000001'::uuid
  AND am.tenant_id = ar.tenant_id
  AND am.status = 1
  AND arm.status <> 1;
