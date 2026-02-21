ALTER TABLE auth_role
    ADD COLUMN IF NOT EXISTS tenant_id UUID;
UPDATE auth_role
SET tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL;
ALTER TABLE auth_role
    ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_auth_role_tenant_id ON auth_role (tenant_id);

ALTER TABLE auth_module
    ADD COLUMN IF NOT EXISTS tenant_id UUID;
UPDATE auth_module
SET tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL;
ALTER TABLE auth_module
    ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_auth_module_tenant_id ON auth_module (tenant_id);

ALTER TABLE inv_category
    ADD COLUMN IF NOT EXISTS tenant_id UUID;
UPDATE inv_category
SET tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL;
ALTER TABLE inv_category
    ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_inv_category_tenant_id ON inv_category (tenant_id);

ALTER TABLE inv_product
    ADD COLUMN IF NOT EXISTS tenant_id UUID;
UPDATE inv_product
SET tenant_id = COALESCE(company_id, '00000000-0000-0000-0000-000000000001')
WHERE tenant_id IS NULL;
ALTER TABLE inv_product
    ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_inv_product_tenant_id ON inv_product (tenant_id);

CREATE INDEX IF NOT EXISTS idx_module_license_tenant_id ON module_license (tenant_id);
