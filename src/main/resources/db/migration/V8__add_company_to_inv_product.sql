-- V6: Agregar company_id a inv_product (idempotente)

-- 1) Columna
ALTER TABLE inv_product
    ADD COLUMN IF NOT EXISTS company_id UUID;

-- 2) Índice para filtros por empresa (opcional)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = 'public' AND indexname = 'idx_inv_product_company'
    ) THEN
        CREATE INDEX idx_inv_product_company ON inv_product(company_id);
    END IF;
END$$;
