ALTER TABLE org_branch
    ADD COLUMN IF NOT EXISTS phone VARCHAR(50),
    ADD COLUMN IF NOT EXISTS email VARCHAR(120),
    ADD COLUMN IF NOT EXISTS manager_name VARCHAR(120);

ALTER TABLE org_warehouse
    ADD COLUMN IF NOT EXISTS warehouse_type VARCHAR(20);

UPDATE org_warehouse
SET warehouse_type = 'MAIN'
WHERE warehouse_type IS NULL;

ALTER TABLE org_warehouse
    ALTER COLUMN warehouse_type SET DEFAULT 'MAIN';

ALTER TABLE org_warehouse
    ALTER COLUMN warehouse_type SET NOT NULL;

INSERT INTO org_warehouse (
    id,
    tenant_id,
    branch_id,
    code,
    name,
    description,
    warehouse_type,
    active,
    address_line1,
    address_line2,
    city,
    state,
    country,
    postal_code,
    latitude,
    longitude,
    location_notes,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    gen_random_uuid(),
    b.tenant_id,
    b.id,
    'MAIN',
    'Bodega Principal',
    'Bodega inicial creada automáticamente',
    'MAIN',
    TRUE,
    b.address_line1,
    b.address_line2,
    b.city,
    b.state,
    COALESCE(b.country, 'Guatemala'),
    b.postal_code,
    b.latitude,
    b.longitude,
    b.location_notes,
    NOW(),
    NOW(),
    'system',
    'system'
FROM org_branch b
WHERE b.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM org_warehouse w
      WHERE w.tenant_id = b.tenant_id
        AND w.branch_id = b.id
        AND UPPER(w.code) = 'MAIN'
        AND w.deleted_at IS NULL
  );
