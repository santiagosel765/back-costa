-- CLIENTES
ALTER TABLE bus_client
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- Si también esperan timestamps por Auditable:
ALTER TABLE bus_client
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE,
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;

-- Repite para las demás tablas business que heredan Auditable
-- Ejemplos típicos (ajustá nombres reales):
-- bus_provider, bus_company, bus_address, pur_purchase, pur_purchase_detail, inv_entry, inv_exit, etc.