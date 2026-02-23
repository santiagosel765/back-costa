-- V106 - Add audit actor columns to remaining tables missing created_by/updated_by
-- Nota: usamos IF NOT EXISTS para que sea idempotente si ya existe en algunos ambientes.

-- AUDIT
ALTER TABLE audit_event
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- JOIN TABLES (muchas veces son tablas puente)
ALTER TABLE auth_role_module
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE auth_user_role
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- BUSINESS TABLES
ALTER TABLE bus_provider
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE bus_purchase
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE bus_purchase_detail
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE bus_quote
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE bus_quote_detail
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- INVENTORY
ALTER TABLE inv_category
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE inv_product
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- TENANT / CATALOGS
ALTER TABLE tenant
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE user_status
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);