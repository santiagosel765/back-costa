-- Remove legacy unique constraints/indexes based on module name normalization.

DROP INDEX IF EXISTS uq_auth_module_tenant_key_ci;
DROP INDEX IF EXISTS uq_auth_module_name;
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'auth_module'::regclass
          AND conname = 'auth_module_name_key'
    ) THEN
        ALTER TABLE auth_module DROP CONSTRAINT auth_module_name_key;
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'auth_module'::regclass
          AND conname = 'uq_auth_module_name'
    ) THEN
        ALTER TABLE auth_module DROP CONSTRAINT uq_auth_module_name;
    END IF;
END
$$;
