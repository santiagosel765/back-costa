-- Seed base status, admin role, and bootstrap administrator user

-- Ensure pgcrypto for gen_random_uuid if needed in future extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1) User status catalog (ACTIVE)
INSERT INTO user_status (status_id, name, description)
VALUES ('6b393ccc-1eba-4075-9fb2-80091d80f87e', 'ACTIVE', 'Usuario activo')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

-- 2) Role catalog (ADMIN)
INSERT INTO auth_role (id, name, description, status)
VALUES ('20bda0bd-c44b-4e46-af5f-d77697a2f7b2', 'ADMIN', 'Administrador del sistema', 1)
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    status = EXCLUDED.status;

-- 3) Administrator user (admin1 / admin123)
WITH active_status AS (
    SELECT status_id FROM user_status WHERE name = 'ACTIVE' LIMIT 1
)
INSERT INTO auth_user (id, username, password, email, full_name, status_id)
SELECT '6cde6b18-4c8b-4429-b4d5-257a0bf8c7b7',
       'admin1',
       '$2b$12$NBU1TlOM1evYLGuigxDkJeiAekov5XZ1DEdMQLTRoBWQHHZyw5rEK',
       'admin1@ferrisys.local',
       'Administrador General',
       active_status.status_id
FROM active_status
ON CONFLICT (username) DO UPDATE
SET password = EXCLUDED.password,
    email = EXCLUDED.email,
    full_name = EXCLUDED.full_name,
    status_id = EXCLUDED.status_id;

-- 4) Link ADMIN role to admin user (idempotent)
WITH admin_user AS (
    SELECT id FROM auth_user WHERE username = 'admin1'
),
admin_role AS (
    SELECT id FROM auth_role WHERE name = 'ADMIN'
),
payload AS (
    SELECT (
               substring(md5(CONCAT(u.id::text, r.id::text)) from 1 for 8) || '-' ||
               substring(md5(CONCAT(u.id::text, r.id::text)) from 9 for 4) || '-' ||
               substring(md5(CONCAT(u.id::text, r.id::text)) from 13 for 4) || '-' ||
               substring(md5(CONCAT(u.id::text, r.id::text)) from 17 for 4) || '-' ||
               substring(md5(CONCAT(u.id::text, r.id::text)) from 21 for 12)
           )::uuid AS generated_id,
           u.id AS user_id,
           r.id AS role_id
    FROM admin_user u
    CROSS JOIN admin_role r
)
INSERT INTO auth_user_role (id, auth_user_id, auth_role_id, status)
SELECT payload.generated_id, payload.user_id, payload.role_id, 1
FROM payload
ON CONFLICT (auth_user_id, auth_role_id) DO UPDATE
SET status = EXCLUDED.status;
