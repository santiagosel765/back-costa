-- V4: Alinear esquema con entidades (idempotente, sin pérdida)

-- Clientes
ALTER TABLE bus_client
    ADD COLUMN IF NOT EXISTS phone    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS address  TEXT;

-- Proveedores
ALTER TABLE bus_provider
    ADD COLUMN IF NOT EXISTS phone    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS address  TEXT,
    ADD COLUMN IF NOT EXISTS ruc      VARCHAR(100);

-- Cotizaciones
ALTER TABLE bus_quote
    ADD COLUMN IF NOT EXISTS quote_date DATE;

-- Compras
ALTER TABLE bus_purchase
    ADD COLUMN IF NOT EXISTS purchase_date DATE;
