-- Seed del catálogo de módulos base.
-- Usa module_key como identificador técnico estable y name como etiqueta visible en español.

DO $$
DECLARE
    v_has_tenant_table BOOLEAN := to_regclass('tenant') IS NOT NULL;
    v_has_tenant_id_column BOOLEAN := EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'auth_module' AND column_name = 'tenant_id'
    );
    v_has_module_key_column BOOLEAN := EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'auth_module' AND column_name = 'module_key'
    );
    v_selected_tenant UUID;
BEGIN
    IF v_has_tenant_table AND v_has_tenant_id_column AND v_has_module_key_column THEN
        SELECT id INTO v_selected_tenant
        FROM tenant
        ORDER BY
            CASE WHEN LOWER(name) IN ('default', 'default-tenant') THEN 0 ELSE 1 END,
            created_at NULLS LAST,
            id
        LIMIT 1;

        INSERT INTO auth_module (id, module_key, name, description, status, tenant_id)
        SELECT m.id, m.module_key, m.name, m.description, 1, v_selected_tenant
        FROM (
            VALUES
                ('e04325e6-3ea8-5e60-b870-9d6b425b3e57'::uuid, 'CORE_AUTH', 'Core de Autenticación', 'Gestión centralizada de identidades, roles y permisos con JWT y auditoría.'),
                ('e4738266-3e79-5b32-8a91-9f2f3ee27aa2'::uuid, 'ORG', 'Organización', 'Estructura jerárquica de compañías, sucursales y bodegas.'),
                ('aabf0b6c-5139-5b24-94cb-33800cc2d754'::uuid, 'MASTER_DATA', 'Datos Maestros', 'Catálogos transversales (unidades, categorías, impuestos).'),
                ('54e263f4-05bd-5fae-8750-70fc37cc44af'::uuid, 'PRODUCTS', 'Productos', 'Definición de SKUs, kits, servicios y atributos técnicos.'),
                ('dedf5c66-052a-51ea-8b5d-09ebf2fcc122'::uuid, 'INVENTORY', 'Inventario', 'Control de existencias, movimientos, lotes y series.'),
                ('655a71e1-192d-5e7f-b10b-57dda2c0dc49'::uuid, 'DOCUMENTS', 'Documentos', 'Plantillas y resguardo de documentos transaccionales.'),
                ('cdfe32d2-b785-5d81-bc19-c84a99cc42bf'::uuid, 'NOTIFICATIONS', 'Notificaciones', 'Motor de alertas por correo, SMS y app.'),
                ('a7919898-113f-59a7-98b0-2fb79872dcea'::uuid, 'CLIENTS', 'Clientes', 'Gestión 360 de clientes y contactos.'),
                ('23d7d86f-b329-5cce-9d03-5e11230d749c'::uuid, 'SUPPLIERS', 'Proveedores', 'Directorio de proveedores, contratos y SLA.'),
                ('01017ceb-b039-5555-b684-243dc3238add'::uuid, 'PRICING', 'Precios', 'Listas, descuentos y políticas comerciales.'),
                ('7d9ff74c-5b0b-52ff-be6d-eaddb0e7c0f1'::uuid, 'SALES', 'Ventas', 'Ciclo comercial completo desde cotización a cierre.'),
                ('9f7ce3be-a79b-5698-8bd3-65f1e67fae3c'::uuid, 'PURCHASE', 'Compras', 'Gestión de órdenes de compra y recepción.'),
                ('ce19f801-01fa-5c17-b07a-540ff045d615'::uuid, 'POS', 'Punto de Venta', 'Interfaz táctil para ventas en mostrador.'),
                ('b87976d0-ddb3-5937-88de-f42fd2c9df3c'::uuid, 'RETURNS', 'Devoluciones', 'Flujos de devoluciones de clientes y proveedores.'),
                ('ae6f5bce-6d7c-5e6f-9d6f-efc32a6dabfd'::uuid, 'WMS', 'WMS', 'Gestión avanzada de almacenes y ubicaciones.'),
                ('a66b5373-22f9-5eaf-b7ba-4a484169463d'::uuid, 'PRODUCTION', 'Producción', 'Órdenes de producción y consumo de materiales.'),
                ('003a9098-2d8e-5d38-a586-74140c67b4c0'::uuid, 'SERVICE_ORDERS', 'Órdenes de Servicio', 'Planificación y ejecución de servicios técnicos.'),
                ('ac5b614a-8340-54e0-86af-c44cc8f0156f'::uuid, 'AR', 'Cuentas por Cobrar', 'Seguimiento de cartera, créditos y cobranzas.'),
                ('789ebf2c-a90b-51e1-8666-23ad45f6857d'::uuid, 'AP', 'Cuentas por Pagar', 'Control de obligaciones con proveedores.'),
                ('e7997db9-a378-5b4f-ad58-6a4955d2e2d0'::uuid, 'ACCOUNTING', 'Contabilidad', 'Libro diario, mayor y estados financieros.'),
                ('cc02d004-4419-5533-948a-754db46b2555'::uuid, 'BANKS', 'Bancos', 'Conciliaciones y movimientos bancarios.'),
                ('e8dcff91-eab3-5c38-a9fe-cbe520f03e90'::uuid, 'REPORTING_BI', 'Reportes y BI', 'Paneles, KPIs y exportaciones.'),
                ('3fb0770d-7cf3-56c5-8696-48e7f04d2c8a'::uuid, 'AUDIT_LOGS', 'Auditoría y Logs', 'Registro de eventos y cambios críticos.'),
                ('e4b0b9b5-3f8a-5e0f-95b2-972d6bce66a6'::uuid, 'WORKFLOWS', 'Workflows', 'Orquestación de procesos y aprobaciones.'),
                ('ebe95d81-ed94-56b6-a96d-513a8f1f5989'::uuid, 'INTEGRATIONS', 'Integraciones', 'Conectores con sistemas externos y marketplace.'),
                ('8b31ce1f-777c-578c-b219-8712c745f1cf'::uuid, 'CONFIG', 'Configuración', 'Preferencias globales, personalización y parámetros.')
        ) AS m(id, module_key, name, description)
        ON CONFLICT (id) DO UPDATE
        SET module_key = EXCLUDED.module_key,
            name = EXCLUDED.name,
            description = EXCLUDED.description,
            status = EXCLUDED.status,
            tenant_id = EXCLUDED.tenant_id;

    ELSIF v_has_tenant_id_column THEN
        -- Compatibilidad con esquemas intermedios sin module_key.
        SELECT id INTO v_selected_tenant
        FROM tenant
        ORDER BY
            CASE WHEN LOWER(name) IN ('default', 'default-tenant') THEN 0 ELSE 1 END,
            created_at NULLS LAST,
            id
        LIMIT 1;

        INSERT INTO auth_module (id, name, description, status, tenant_id)
        VALUES
            ('e04325e6-3ea8-5e60-b870-9d6b425b3e57', 'Core de Autenticación', 'Gestión centralizada de identidades, roles y permisos con JWT y auditoría.', 1, v_selected_tenant),
            ('e4738266-3e79-5b32-8a91-9f2f3ee27aa2', 'Organización', 'Estructura jerárquica de compañías, sucursales y bodegas.', 1, v_selected_tenant)
        ON CONFLICT (id) DO UPDATE
        SET name = EXCLUDED.name,
            description = EXCLUDED.description,
            status = EXCLUDED.status,
            tenant_id = EXCLUDED.tenant_id;

    ELSE
        -- Compatibilidad con baseline histórico previo a multi-tenant.
        INSERT INTO auth_module (id, name, description, status)
        VALUES
            ('e04325e6-3ea8-5e60-b870-9d6b425b3e57', 'Core de Autenticación', 'Gestión centralizada de identidades, roles y permisos con JWT y auditoría.', 1),
            ('e4738266-3e79-5b32-8a91-9f2f3ee27aa2', 'Organización', 'Estructura jerárquica de compañías, sucursales y bodegas.', 1)
        ON CONFLICT (id) DO UPDATE
        SET name = EXCLUDED.name,
            description = EXCLUDED.description,
            status = EXCLUDED.status;
    END IF;
END
$$;
