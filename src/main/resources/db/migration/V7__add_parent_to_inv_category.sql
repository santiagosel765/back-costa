-- V5: Agregar parent_category_id a inv_category (idempotente)

-- 1) Columna
ALTER TABLE inv_category
    ADD COLUMN IF NOT EXISTS parent_category_id UUID;

-- 2) FK self-reference (si no existe)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        WHERE t.relname = 'inv_category'
          AND c.conname = 'fk_inv_category_parent'
    ) THEN
        ALTER TABLE inv_category
            ADD CONSTRAINT fk_inv_category_parent
            FOREIGN KEY (parent_category_id)
            REFERENCES inv_category (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL;
    END IF;
END$$;

-- 3) Índice para lookups por padre (opcional)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = 'public' AND indexname = 'idx_inv_category_parent'
    ) THEN
        CREATE INDEX idx_inv_category_parent ON inv_category(parent_category_id);
    END IF;
END$$;
