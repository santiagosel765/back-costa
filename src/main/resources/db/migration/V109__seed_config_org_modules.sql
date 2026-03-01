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
INSERT INTO auth_module (id, name, description, status, tenant_id)
SELECT modules.id,
       modules.name,
       modules.description,
       modules.status,
       selected_tenant.id
FROM selected_tenant
CROSS JOIN (
    VALUES
        ('1f4f06e2-4438-4f48-b16d-26cf27f1064f'::UUID, 'CONFIG', 'Configuración y parámetros maestros', 1),
        ('9dbf5cf0-4937-4f30-a8e8-0e67ba2098dd'::UUID, 'ORG', 'Gestión organizacional de sucursales y bodegas', 1)
) AS modules (id, name, description, status)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    tenant_id = EXCLUDED.tenant_id;
