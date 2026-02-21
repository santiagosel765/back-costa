-- Seed the catalog of core modules based on docs/modules_catalog.yaml

INSERT INTO auth_module (id, name, description, status)
VALUES
    ('e04325e6-3ea8-5e60-b870-9d6b425b3e57', 'CORE_AUTH', 'Gestión centralizada de identidades, roles y permisos con JWT y auditoría.', 1),
    ('e4738266-3e79-5b32-8a91-9f2f3ee27aa2', 'ORG_BRANCH', 'Estructura jerárquica de compañías, sucursales y bodegas.', 1),
    ('aabf0b6c-5139-5b24-94cb-33800cc2d754', 'MASTER_DATA', 'Catálogos transversales (unidades, categorías, impuestos).', 1),
    ('54e263f4-05bd-5fae-8750-70fc37cc44af', 'PRODUCTS', 'Definición de SKUs, kits, servicios y atributos técnicos.', 1),
    ('dedf5c66-052a-51ea-8b5d-09ebf2fcc122', 'INVENTORY', 'Control de existencias, movimientos, lotes y series.', 1),
    ('655a71e1-192d-5e7f-b10b-57dda2c0dc49', 'DOCUMENTS', 'Plantillas y resguardo de documentos transaccionales.', 1),
    ('cdfe32d2-b785-5d81-bc19-c84a99cc42bf', 'NOTIFICATIONS', 'Motor de alertas por correo, SMS y app.', 1),
    ('a7919898-113f-59a7-98b0-2fb79872dcea', 'CLIENTS', 'Gestión 360 de clientes y contactos.', 1),
    ('23d7d86f-b329-5cce-9d03-5e11230d749c', 'SUPPLIERS', 'Directorio de proveedores, contratos y SLA.', 1),
    ('01017ceb-b039-5555-b684-243dc3238add', 'PRICING', 'Listas, descuentos y políticas comerciales.', 1),
    ('7d9ff74c-5b0b-52ff-be6d-eaddb0e7c0f1', 'SALES', 'Ciclo comercial completo desde cotización a cierre.', 1),
    ('9f7ce3be-a79b-5698-8bd3-65f1e67fae3c', 'PURCHASE', 'Gestión de órdenes de compra y recepción.', 1),
    ('ce19f801-01fa-5c17-b07a-540ff045d615', 'POS', 'Interfaz táctil para ventas en mostrador.', 1),
    ('b87976d0-ddb3-5937-88de-f42fd2c9df3c', 'RETURNS', 'Flujos de devoluciones de clientes y proveedores.', 1),
    ('ae6f5bce-6d7c-5e6f-9d6f-efc32a6dabfd', 'WMS', 'Gestión avanzada de almacenes y ubicaciones.', 1),
    ('a66b5373-22f9-5eaf-b7ba-4a484169463d', 'PRODUCTION', 'Órdenes de producción y consumo de materiales.', 1),
    ('003a9098-2d8e-5d38-a586-74140c67b4c0', 'SERVICE_ORDERS', 'Planificación y ejecución de servicios técnicos.', 1),
    ('ac5b614a-8340-54e0-86af-c44cc8f0156f', 'AR', 'Seguimiento de cartera, créditos y cobranzas.', 1),
    ('789ebf2c-a90b-51e1-8666-23ad45f6857d', 'AP', 'Control de obligaciones con proveedores.', 1),
    ('e7997db9-a378-5b4f-ad58-6a4955d2e2d0', 'ACCOUNTING', 'Libro diario, mayor y estados financieros.', 1),
    ('cc02d004-4419-5533-948a-754db46b2555', 'BANKS', 'Conciliaciones y movimientos bancarios.', 1),
    ('e8dcff91-eab3-5c38-a9fe-cbe520f03e90', 'REPORTING_BI', 'Paneles, KPIs y exportaciones.', 1),
    ('3fb0770d-7cf3-56c5-8696-48e7f04d2c8a', 'AUDIT_LOGS', 'Registro de eventos y cambios críticos.', 1),
    ('e4b0b9b5-3f8a-5e0f-95b2-972d6bce66a6', 'WORKFLOWS', 'Orquestación de procesos y aprobaciones.', 1),
    ('ebe95d81-ed94-56b6-a96d-513a8f1f5989', 'INTEGRATIONS', 'Conectores con sistemas externos y marketplace.', 1),
    ('8b31ce1f-777c-578c-b219-8712c745f1cf', 'SETTINGS', 'Preferencias globales, personalización y parámetros.', 1)
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    status = EXCLUDED.status;
