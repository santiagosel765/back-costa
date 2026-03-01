-- Deduplicate active modules by tenant/module_key and repoint role-module assignments.

WITH ranked AS (
    SELECT am.id,
           am.tenant_id,
           am.module_key,
           ROW_NUMBER() OVER (
               PARTITION BY am.tenant_id, am.module_key
               ORDER BY
                   CASE WHEN am.name ~ '^[A-Z0-9_]+$' THEN 1 ELSE 0 END,
                   CASE WHEN am.name ~ '[a-záéíóúñ ]' THEN 0 ELSE 1 END,
                   am.updated_at DESC NULLS LAST,
                   am.created_at DESC NULLS LAST,
                   am.id
           ) AS row_num
    FROM auth_module am
    WHERE am.status = 1
)
UPDATE auth_module am
SET status = 0
FROM ranked r
WHERE am.id = r.id
  AND r.row_num > 1;

WITH canonical AS (
    SELECT DISTINCT ON (am.tenant_id, am.module_key)
           am.id AS canonical_id,
           am.tenant_id,
           am.module_key
    FROM auth_module am
    WHERE am.status = 1
    ORDER BY
        am.tenant_id,
        am.module_key,
        CASE WHEN am.name ~ '^[A-Z0-9_]+$' THEN 1 ELSE 0 END,
        CASE WHEN am.name ~ '[a-záéíóúñ ]' THEN 0 ELSE 1 END,
        am.updated_at DESC NULLS LAST,
        am.created_at DESC NULLS LAST,
        am.id
),
targets AS (
    SELECT arm.id,
           arm.tenant_id,
           arm.auth_role_id,
           arm.auth_module_id,
           c.canonical_id,
           ROW_NUMBER() OVER (
               PARTITION BY arm.tenant_id, arm.auth_role_id, c.canonical_id
               ORDER BY
                   CASE WHEN arm.status = 1 THEN 0 ELSE 1 END,
                   arm.updated_at DESC NULLS LAST,
                   arm.created_at DESC NULLS LAST,
                   arm.id
           ) AS row_num
    FROM auth_role_module arm
    JOIN auth_module am ON am.id = arm.auth_module_id
    JOIN canonical c
      ON c.tenant_id = am.tenant_id
     AND c.module_key = am.module_key
)
DELETE FROM auth_role_module arm
USING targets t
WHERE arm.id = t.id
  AND t.row_num > 1;

WITH canonical AS (
    SELECT DISTINCT ON (am.tenant_id, am.module_key)
           am.id AS canonical_id,
           am.tenant_id,
           am.module_key
    FROM auth_module am
    WHERE am.status = 1
    ORDER BY
        am.tenant_id,
        am.module_key,
        CASE WHEN am.name ~ '^[A-Z0-9_]+$' THEN 1 ELSE 0 END,
        CASE WHEN am.name ~ '[a-záéíóúñ ]' THEN 0 ELSE 1 END,
        am.updated_at DESC NULLS LAST,
        am.created_at DESC NULLS LAST,
        am.id
)
UPDATE auth_role_module arm
SET auth_module_id = c.canonical_id
FROM auth_module am
JOIN canonical c
  ON c.tenant_id = am.tenant_id
 AND c.module_key = am.module_key
WHERE arm.auth_module_id = am.id
  AND arm.auth_module_id <> c.canonical_id;

-- Safety net in case duplicates remain after repoint.
WITH ranked_links AS (
    SELECT arm.id,
           ROW_NUMBER() OVER (
               PARTITION BY arm.tenant_id, arm.auth_role_id, arm.auth_module_id
               ORDER BY
                   CASE WHEN arm.status = 1 THEN 0 ELSE 1 END,
                   arm.updated_at DESC NULLS LAST,
                   arm.created_at DESC NULLS LAST,
                   arm.id
           ) AS row_num
    FROM auth_role_module arm
)
DELETE FROM auth_role_module arm
USING ranked_links rl
WHERE arm.id = rl.id
  AND rl.row_num > 1;
