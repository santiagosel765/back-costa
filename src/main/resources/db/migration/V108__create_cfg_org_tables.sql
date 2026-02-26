CREATE TABLE IF NOT EXISTS cfg_currency (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_cfg_currency_tenant_active ON cfg_currency (tenant_id, active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cfg_currency_tenant_code ON cfg_currency (tenant_id, code);

CREATE TABLE IF NOT EXISTS cfg_tax (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    rate NUMERIC(10, 4) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_cfg_tax_tenant_active ON cfg_tax (tenant_id, active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cfg_tax_tenant_code ON cfg_tax (tenant_id, code);

CREATE TABLE IF NOT EXISTS cfg_parameter (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(150) NOT NULL,
    name VARCHAR(150),
    description TEXT,
    value VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_cfg_parameter_tenant_active ON cfg_parameter (tenant_id, active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cfg_parameter_tenant_code ON cfg_parameter (tenant_id, code);

CREATE TABLE IF NOT EXISTS cfg_payment_method (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_cfg_payment_method_tenant_active ON cfg_payment_method (tenant_id, active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cfg_payment_method_tenant_code ON cfg_payment_method (tenant_id, code);

CREATE TABLE IF NOT EXISTS cfg_document_type (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_cfg_document_type_tenant_active ON cfg_document_type (tenant_id, active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cfg_document_type_tenant_code ON cfg_document_type (tenant_id, code);

CREATE TABLE IF NOT EXISTS org_branch (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_org_branch_tenant_active ON org_branch (tenant_id, active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_org_branch_tenant_code ON org_branch (tenant_id, code);

CREATE TABLE IF NOT EXISTS org_warehouse (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_org_warehouse_branch FOREIGN KEY (branch_id)
      REFERENCES org_branch(id)
      ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_org_warehouse_tenant_branch_active ON org_warehouse (tenant_id, branch_id, active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_org_warehouse_tenant_code ON org_warehouse (tenant_id, code);

CREATE TABLE IF NOT EXISTS org_user_branch (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_org_user_branch_branch FOREIGN KEY (branch_id)
      REFERENCES org_branch(id)
      ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_org_user_branch_tenant_user_active ON org_user_branch (tenant_id, user_id, active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_org_user_branch_tenant_user_branch ON org_user_branch (tenant_id, user_id, branch_id);
