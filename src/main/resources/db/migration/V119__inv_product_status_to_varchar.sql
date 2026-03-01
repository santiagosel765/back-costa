-- Migrates legacy inv_product.status INTEGER values to VARCHAR enum values used by ProductStatus.
-- Compatibility mapping assumption: 0=DRAFT, 1=ACTIVE, 2=ARCHIVED, NULL/other => DRAFT.
DO $$
DECLARE
    v_status_type text;
BEGIN
    SELECT c.data_type
      INTO v_status_type
      FROM information_schema.columns c
     WHERE c.table_schema = current_schema()
       AND c.table_name = 'inv_product'
       AND c.column_name = 'status';

    -- If status is already character based, only normalize nullability/default and finish.
    IF v_status_type IN ('character varying', 'text', 'character') THEN
        EXECUTE 'UPDATE inv_product SET status = COALESCE(NULLIF(status, ''''''), ''DRAFT'')';
        EXECUTE 'ALTER TABLE inv_product ALTER COLUMN status TYPE VARCHAR(32) USING status::varchar';
        EXECUTE 'ALTER TABLE inv_product ALTER COLUMN status SET DEFAULT ''DRAFT''';
        EXECUTE 'ALTER TABLE inv_product ALTER COLUMN status SET NOT NULL';
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
          FROM information_schema.columns c
         WHERE c.table_schema = current_schema()
           AND c.table_name = 'inv_product'
           AND c.column_name = 'status_v2'
    ) THEN
        EXECUTE 'ALTER TABLE inv_product DROP COLUMN status_v2';
    END IF;

    EXECUTE 'ALTER TABLE inv_product ADD COLUMN status_v2 VARCHAR(32) DEFAULT ''DRAFT''';

    -- Legacy mapping:
    -- NULL -> DRAFT, 0 -> DRAFT, 1 -> ACTIVE, 2 -> ARCHIVED, fallback -> DRAFT.
    EXECUTE $stmt$
        UPDATE inv_product
           SET status_v2 = CASE
               WHEN status IS NULL THEN 'DRAFT'
               WHEN status = 0 THEN 'DRAFT'
               WHEN status = 1 THEN 'ACTIVE'
               WHEN status = 2 THEN 'ARCHIVED'
               ELSE 'DRAFT'
           END
    $stmt$;

    IF EXISTS (
        SELECT 1
          FROM information_schema.columns c
         WHERE c.table_schema = current_schema()
           AND c.table_name = 'inv_product'
           AND c.column_name = 'status'
    ) THEN
        EXECUTE 'ALTER TABLE inv_product DROP COLUMN status';
    END IF;

    EXECUTE 'ALTER TABLE inv_product RENAME COLUMN status_v2 TO status';
    EXECUTE 'ALTER TABLE inv_product ALTER COLUMN status SET DEFAULT ''DRAFT''';
    EXECUTE 'ALTER TABLE inv_product ALTER COLUMN status SET NOT NULL';
END $$;
