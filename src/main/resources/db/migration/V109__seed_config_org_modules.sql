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
        ('8b31ce1f-777c-578c-b219-8712c745f1cf'::UUID, 'CONFIG', 'Configuración y parámetros maestros', 1),
        ('e4738266-3e79-5b32-8a91-9f2f3ee27aa2'::UUID, 'ORG', 'Gestión organizacional de sucursales y bodegas', 1)
) AS modules (id, name, description, status)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    tenant_id = EXCLUDED.tenant_id;
