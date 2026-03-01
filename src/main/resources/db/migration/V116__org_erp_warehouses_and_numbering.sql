ALTER TABLE org_branch
    ADD COLUMN IF NOT EXISTS address_line1 VARCHAR(120),
    ADD COLUMN IF NOT EXISTS address_line2 VARCHAR(120),
    ADD COLUMN IF NOT EXISTS city VARCHAR(80),
    ADD COLUMN IF NOT EXISTS state VARCHAR(80),
    ADD COLUMN IF NOT EXISTS country VARCHAR(80),
    ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS latitude NUMERIC(10, 7),
    ADD COLUMN IF NOT EXISTS longitude NUMERIC(10, 7),
    ADD COLUMN IF NOT EXISTS location_notes TEXT;

UPDATE org_branch
SET address_line1 = COALESCE(address_line1, address)
WHERE address IS NOT NULL
  AND address_line1 IS NULL;

UPDATE org_branch
SET country = COALESCE(country, 'Guatemala')
WHERE country IS NULL;

ALTER TABLE org_warehouse
    ADD COLUMN IF NOT EXISTS address_line1 VARCHAR(120),
    ADD COLUMN IF NOT EXISTS address_line2 VARCHAR(120),
    ADD COLUMN IF NOT EXISTS city VARCHAR(80),
    ADD COLUMN IF NOT EXISTS state VARCHAR(80),
    ADD COLUMN IF NOT EXISTS country VARCHAR(80),
    ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS latitude NUMERIC(10, 7),
    ADD COLUMN IF NOT EXISTS longitude NUMERIC(10, 7),
    ADD COLUMN IF NOT EXISTS location_notes TEXT;

DROP INDEX IF EXISTS uq_org_warehouse_tenant_code;
CREATE UNIQUE INDEX IF NOT EXISTS uq_org_warehouse_tenant_branch_code ON org_warehouse (tenant_id, branch_id, code);
CREATE INDEX IF NOT EXISTS idx_org_warehouse_tenant_code ON org_warehouse (tenant_id, code);

CREATE TABLE IF NOT EXISTS org_document_numbering (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    document_type_id UUID NOT NULL,
    series VARCHAR(20) NOT NULL,
    next_number INT NOT NULL CHECK (next_number >= 1),
    padding INT NOT NULL DEFAULT 8 CHECK (padding >= 1 AND padding <= 20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_org_document_numbering_branch FOREIGN KEY (branch_id)
      REFERENCES org_branch(id)
      ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_org_document_numbering_document_type FOREIGN KEY (document_type_id)
      REFERENCES cfg_document_type(id)
      ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_org_document_numbering_tenant_branch_type_series
    ON org_document_numbering (tenant_id, branch_id, document_type_id, series);
CREATE INDEX IF NOT EXISTS idx_org_document_numbering_tenant_branch_active
    ON org_document_numbering (tenant_id, branch_id, active);

INSERT INTO org_warehouse (
    id,
    tenant_id,
    branch_id,
    code,
    name,
    description,
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
    TRUE,
    b.address_line1,
    b.address_line2,
    b.city,
    b.state,
    b.country,
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
    b.tenant_id,
    b.id,
    dt.id,
    CASE
        WHEN LOWER(dt.name) LIKE '%factura%' THEN 'F001'
        WHEN LOWER(dt.name) LIKE '%boleta%' THEN 'B001'
        WHEN LOWER(dt.name) LIKE '%nota de crédito%' OR LOWER(dt.name) LIKE '%nota de credito%' THEN 'NC01'
        ELSE 'S001'
    END AS series,
    1,
    8,
    TRUE,
    NOW(),
    NOW(),
    'system',
    'system'
FROM org_branch b
JOIN cfg_document_type dt
  ON dt.tenant_id = b.tenant_id
 AND dt.active = TRUE
 AND dt.deleted_at IS NULL
WHERE b.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM org_document_numbering dn
      WHERE dn.tenant_id = b.tenant_id
        AND dn.branch_id = b.id
        AND dn.document_type_id = dt.id
        AND dn.series = CASE
            WHEN LOWER(dt.name) LIKE '%factura%' THEN 'F001'
            WHEN LOWER(dt.name) LIKE '%boleta%' THEN 'B001'
            WHEN LOWER(dt.name) LIKE '%nota de crédito%' OR LOWER(dt.name) LIKE '%nota de credito%' THEN 'NC01'
            ELSE 'S001'
        END
        AND dn.deleted_at IS NULL
  );
