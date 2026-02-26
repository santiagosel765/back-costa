INSERT INTO auth_module (id, name, description, status)
VALUES ('1f4f06e2-4438-4f48-b16d-26cf27f1064f', 'config', 'Configuración y parámetros maestros', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;

INSERT INTO auth_module (id, name, description, status)
VALUES ('9dbf5cf0-4937-4f30-a8e8-0e67ba2098dd', 'org', 'Gestión organizacional de sucursales y bodegas', 1)
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, status = EXCLUDED.status;
