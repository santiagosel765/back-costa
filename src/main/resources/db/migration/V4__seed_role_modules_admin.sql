-- Assign every active module to the ADMIN role

WITH admin_role AS (
    SELECT id FROM auth_role WHERE name = 'ADMIN'
),
active_modules AS (
    SELECT id FROM auth_module WHERE status = 1
),
combos AS (
    SELECT (
               substring(md5(CONCAT(ar.id::text, am.id::text)) from 1 for 8) || '-' ||
               substring(md5(CONCAT(ar.id::text, am.id::text)) from 9 for 4) || '-' ||
               substring(md5(CONCAT(ar.id::text, am.id::text)) from 13 for 4) || '-' ||
               substring(md5(CONCAT(ar.id::text, am.id::text)) from 17 for 4) || '-' ||
               substring(md5(CONCAT(ar.id::text, am.id::text)) from 21 for 12)
           )::uuid AS generated_id,
           ar.id AS role_id,
           am.id AS module_id
    FROM admin_role ar
    CROSS JOIN active_modules am
)
INSERT INTO auth_role_module (id, auth_role_id, auth_module_id, status)
SELECT combos.generated_id, combos.role_id, combos.module_id, 1
FROM combos
ON CONFLICT (auth_role_id, auth_module_id) DO UPDATE
SET status = EXCLUDED.status;
