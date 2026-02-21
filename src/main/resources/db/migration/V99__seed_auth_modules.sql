-- Seed de módulos base Ferrisys (idempotente)
-- Si el módulo ya existe, se actualiza la descripción y el estado.

INSERT INTO auth_module (id, name, description, status)
VALUES ('437f2d7c-41cc-5faa-9a7e-dda23986e9bc', 'Core de Autenticación', 'Gestión centralizada de identidades, roles y permisos con JWT y auditoría.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('bd021a5e-fee5-5b21-a452-c1460261c3a3', 'Sucursales y Organizaciones', 'Estructura jerárquica de compañías, sucursales y bodegas.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('b8adeb99-a34e-5e09-aefe-a17e311c592d', 'Datos Maestros', 'Catálogos transversales (unidades, categorías, impuestos).', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('487ccbf6-c5b5-5782-abfc-13af8597f360', 'Productos y Servicios', 'Definición de SKUs, kits, servicios y atributos técnicos.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('ad3b7f45-e2bc-55cc-9164-59c44ad94503', 'Inventario', 'Control de existencias, movimientos, lotes y series.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('007275af-cd62-5fee-89ff-43100b62610e', 'Gestión Documental', 'Plantillas y resguardo de documentos transaccionales.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('be7b26d3-4643-56f4-809b-fbebc467020d', 'Notificaciones', 'Motor de alertas por correo, SMS y app.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('aef5962a-721a-5232-8519-3417a64ebe94', 'Clientes', 'Gestión 360 de clientes y contactos.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('e7ba81ea-548f-56e4-8ea6-211d316e49fb', 'Proveedores', 'Directorio de proveedores, contratos y SLA.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('fe6c7f7e-2757-56e9-bafe-fa084b29a115', 'Gestión de Precios', 'Listas, descuentos y políticas comerciales.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('494e94f6-4384-597d-972c-f86126ece4da', 'Ventas', 'Ciclo comercial completo desde cotización a cierre.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('5eea987a-0dcb-545a-b4ea-c80b7f375881', 'Compras', 'Gestión de órdenes de compra y recepción.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('482dd5f6-87c5-54fb-9c0b-c352e224c5ed', 'Punto de Venta', 'Interfaz táctil para ventas en mostrador.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('a50b3a12-4b5a-532a-b4f5-fadb4ce4c120', 'Devoluciones', 'Flujos de devoluciones de clientes y proveedores.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('e37fc3b8-f9d5-533f-9290-cf1557912753', 'WMS', 'Gestión avanzada de almacenes y ubicaciones.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('1bf45565-843e-50b4-bd28-d79e7edb2d19', 'Producción', 'Órdenes de producción y consumo de materiales.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('bf8b5873-c9e4-5d64-a646-0d973a1c18c1', 'Órdenes de Servicio', 'Planificación y ejecución de servicios técnicos.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('d8be8d15-fcd8-5107-b651-8a3764599e90', 'Cuentas por Cobrar', 'Seguimiento de cartera, créditos y cobranzas.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('12c8c4e4-e3ab-5917-a83b-783c517a78a0', 'Cuentas por Pagar', 'Control de obligaciones con proveedores.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('24a2747a-b1fc-5aa7-936f-58647e8cfc36', 'Contabilidad', 'Libro diario, mayor y estados financieros.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('592b1e88-083d-538a-8122-d55d3883e533', 'Bancos', 'Conciliaciones y movimientos bancarios.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('8b0ef1eb-95d0-51e1-8ef5-53a123876690', 'Reportes y BI', 'Paneles, KPIs y exportaciones.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('f6e9e1e2-37c1-52ee-b44b-f74f9c9173bf', 'Auditoría y Logs', 'Registro de eventos y cambios críticos.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('5669999b-11f2-52c5-9f53-c158cf26c962', 'Workflows', 'Orquestación de procesos y aprobaciones.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('46f3b8cd-a4f0-54a8-b72d-557ece6d3c28', 'Integraciones', 'Conectores con sistemas externos y marketplace.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('0c7d1e4b-51b3-56d6-b243-712ce21c2186', 'Configuración', 'Preferencias globales, personalización y parámetros.', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;
