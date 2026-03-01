-- S2 master data + product catalog

CREATE TABLE IF NOT EXISTS mst_category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    code VARCHAR(64),
    name VARCHAR(160) NOT NULL,
    parent_id UUID NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120),
    CONSTRAINT fk_mst_category_parent FOREIGN KEY (parent_id) REFERENCES mst_category(id)
);
CREATE INDEX IF NOT EXISTS idx_mst_category_tenant ON mst_category(tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_mst_category_name_active
    ON mst_category(tenant_id, lower(name))
    WHERE deleted_at IS NULL AND active = TRUE;

CREATE TABLE IF NOT EXISTS mst_brand (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    code VARCHAR(64),
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_mst_brand_name_active
    ON mst_brand(tenant_id, lower(name))
    WHERE deleted_at IS NULL AND active = TRUE;

CREATE TABLE IF NOT EXISTS mst_uom_group (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS mst_uom (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    group_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    is_base BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120),
    CONSTRAINT fk_mst_uom_group FOREIGN KEY (group_id) REFERENCES mst_uom_group(id)
);

CREATE TABLE IF NOT EXISTS mst_uom_conversion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    group_id UUID NOT NULL,
    from_uom_id UUID NOT NULL,
    to_uom_id UUID NOT NULL,
    factor NUMERIC(18,6) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120),
    CONSTRAINT fk_mst_uom_conv_group FOREIGN KEY (group_id) REFERENCES mst_uom_group(id),
    CONSTRAINT fk_mst_uom_conv_from FOREIGN KEY (from_uom_id) REFERENCES mst_uom(id),
    CONSTRAINT fk_mst_uom_conv_to FOREIGN KEY (to_uom_id) REFERENCES mst_uom(id),
    CONSTRAINT chk_mst_uom_conversion_factor CHECK (factor > 0)
);

CREATE TABLE IF NOT EXISTS mst_attribute (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(20) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    searchable BOOLEAN NOT NULL DEFAULT FALSE,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    apply_to VARCHAR(20) NOT NULL DEFAULT 'PRODUCT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_mst_attribute_code_active
    ON mst_attribute(tenant_id, lower(code))
    WHERE deleted_at IS NULL AND active = TRUE;

CREATE TABLE IF NOT EXISTS mst_attribute_option (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    attribute_id UUID NOT NULL,
    value VARCHAR(160) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120),
    CONSTRAINT fk_mst_attribute_option_attribute FOREIGN KEY (attribute_id) REFERENCES mst_attribute(id)
);

CREATE TABLE IF NOT EXISTS mst_tax_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_mst_tax_profile_name_active
    ON mst_tax_profile(tenant_id, lower(name))
    WHERE deleted_at IS NULL AND active = TRUE;

CREATE TABLE IF NOT EXISTS mst_tax_profile_tax (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    tax_profile_id UUID NOT NULL,
    tax_id UUID NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    inclusive BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120),
    CONSTRAINT fk_mst_tax_profile_tax_profile FOREIGN KEY (tax_profile_id) REFERENCES mst_tax_profile(id),
    CONSTRAINT fk_mst_tax_profile_tax_tax FOREIGN KEY (tax_id) REFERENCES cfg_tax(id)
);

ALTER TABLE inv_product
    ADD COLUMN IF NOT EXISTS type VARCHAR(20) DEFAULT 'PRODUCT',
    ADD COLUMN IF NOT EXISTS sku VARCHAR(80),
    ADD COLUMN IF NOT EXISTS brand_id UUID,
    ADD COLUMN IF NOT EXISTS uom_id UUID,
    ADD COLUMN IF NOT EXISTS tax_profile_id UUID,
    ADD COLUMN IF NOT EXISTS base_price NUMERIC(18,4),
    ADD COLUMN IF NOT EXISTS track_stock BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS track_lot BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS track_serial BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(120);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inv_product_brand') THEN
        ALTER TABLE inv_product ADD CONSTRAINT fk_inv_product_brand FOREIGN KEY (brand_id) REFERENCES mst_brand(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inv_product_uom') THEN
        ALTER TABLE inv_product ADD CONSTRAINT fk_inv_product_uom FOREIGN KEY (uom_id) REFERENCES mst_uom(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inv_product_tax_profile') THEN
        ALTER TABLE inv_product ADD CONSTRAINT fk_inv_product_tax_profile FOREIGN KEY (tax_profile_id) REFERENCES mst_tax_profile(id);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_inv_product_sku_active
    ON inv_product(tenant_id, lower(sku))
    WHERE sku IS NOT NULL AND deleted_at IS NULL AND active = TRUE;

CREATE TABLE IF NOT EXISTS inv_product_attribute_value (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL,
    attribute_id UUID NOT NULL,
    value_text TEXT,
    value_number NUMERIC(18,6),
    value_bool BOOLEAN,
    value_date DATE,
    option_id UUID,
    value_json JSONB,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(120),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(120),
    updated_by VARCHAR(120),
    CONSTRAINT fk_inv_product_attribute_value_product FOREIGN KEY (product_id) REFERENCES inv_product(id),
    CONSTRAINT fk_inv_product_attribute_value_attribute FOREIGN KEY (attribute_id) REFERENCES mst_attribute(id),
    CONSTRAINT fk_inv_product_attribute_value_option FOREIGN KEY (option_id) REFERENCES mst_attribute_option(id)
);
