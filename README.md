# 🔐 ferrisys-auth

Authentication and authorization microservice for the **Ferrisys System**, a scalable and modular platform for hardware store management.

---

## 📌 Prerequisites

Ensure the following modules are already present and compiled:

### 📦 [ferrisys-common](../ferrisys-common)

> Contains shared entities (User, Role, Company, Module), DTOs, enums, and audit base classes.

### 📦 [ferrisys-parent](../ferrisys-parent)

> Parent POM for centralized dependency and plugin versions using Java 17 and Spring Boot 3.4.

---

## 🎯 Features

- 👤 **User Registration** with role and company assignment
- 🔐 **JWT-based login authentication**
- 🔄 **Password update/change**
- 🧩 **Role-based access** to system modules
- 🗂️ Uses centralized entities from `ferrisys-common`
- 📜 Loads default roles and module mappings from `data.sql`

---

## 📂 Project Structure

## ⚙️ Configuration

Set the required environment variables before running the service so credentials and secrets are not committed to the repository.

### Windows PowerShell

```powershell
Set-Item -Path Env:SPRING_DATASOURCE_URL -Value "jdbc:postgresql://HOST:5432/postgres?sslmode=require"
Set-Item -Path Env:SPRING_DATASOURCE_USERNAME -Value "postgres"
Set-Item -Path Env:SPRING_DATASOURCE_PASSWORD -Value "your_password"
Set-Item -Path Env:JWT_SECRET -Value "CHANGEME_32CHARS"
```

### Bash / Unix shells

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://HOST:5432/postgres?sslmode=require"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="your_password"
export JWT_SECRET="CHANGEME_32CHARS"
```

For local development you can copy [`application-local.yml.sample`](src/main/resources/application-local.yml.sample) to `application-local.yml` and adjust the values according to your environment.

---

## 🚀 Local development

The repository includes helper scripts that start the backend and frontend together so you can develop against both modules at the same time:

- **Bash / Unix shells**: run `../scripts/run-all.sh` from this directory (or `scripts/run-all.sh` from the repository root).
- **Windows PowerShell**: run `..\scripts\run-all.ps1` from this directory (or `scripts\run-all.ps1` from the repository root).

Both scripts install missing frontend dependencies, launch `./mvnw spring-boot:run` for this service, and start the Angular dev server. Use `Ctrl+C` to stop both processes.

### Flyway migration policy (dev note)

- **No modificar migraciones ya aplicadas.**
- Si una migración aplicada se modifica accidentalmente en local, corregir restaurando el archivo original y luego ejecutar `mvn flyway:repair` **de forma manual** o reiniciar la base de datos de desarrollo.


---

## 🔎 Auth context contract (`/v1/auth/me/context`)

After login, the frontend can call `GET /v1/auth/me/context` with a Bearer token to bootstrap SaaS context in one request:

- `user`: identity/profile basics, with stable status fields (`status`, `statusKey`, `statusId`, `statusLabel`).
- `tenant`: tenant id and metadata when available.
- `roles`: active role names.
- `modules`: tenant+role enabled modules with normalized `key`, human `label`, and optional `expiresAt` from license.
- `permissions`: capability map by module key (for example `{"INVENTARIO": ["read"]}`).
- `token`: refreshed JWT (`accessToken`) plus `expiresAt` metadata.
- `serverTime`: backend timestamp for client-side drift checks.

JWT claims now include:

- `tenant_id`
- `roles`
- `modules`

> Security note: claims are for UX/context only. Backend authorization and license checks remain enforced server-side.

### Response sample

```json
{
  "user": {
    "id": "6cde6b18-4c8b-4429-b4d5-257a0bf8c7b7",
    "username": "admin1",
    "fullName": "Administrador General",
    "email": "admin1@ferrisys.local",
    "status": "ACTIVE",
    "statusId": "6b393ccc-1eba-4075-9fb2-80091d80f87e",
    "statusKey": "ACTIVE",
    "statusLabel": "Usuario activo"
  },
  "tenant": {
    "tenantId": "<tenant-uuid>",
    "name": "tenant-admin1",
    "status": 1
  },
  "roles": ["ADMIN"],
  "modules": [
    {
      "key": "INVENTARIO",
      "label": "Inventario",
      "enabled": true,
      "expiresAt": null
    }
  ],
  "permissions": {
    "INVENTARIO": ["read", "write"]
  },
  "token": {
    "accessToken": "<jwt>",
    "expiresAt": "2026-01-01T00:00:00Z"
  },
  "serverTime": "2026-01-01T00:00:00Z"
}
```

### Manual QA

1. Run backend.
2. `POST /v1/auth/login` with `admin1/admin123` and copy `token`.
3. `GET /v1/auth/me/context` with `Authorization: Bearer <token>`:
   - expect `200`
   - expect `user.status = "ACTIVE"` (or another stable key), not a random UUID value.
   - expect `user.statusId` present for traceability.
   - expect non-empty `roles[]`, `modules[]`, and non-null `permissions`.
4. Test with a non-admin user:
   - `permissions` should map each enabled module to at least `["read"]`.
5. Disable one module license for the same tenant (UI/admin SQL) and repeat step 3:
   - disabled module must no longer be present in `modules[]` and in `permissions`.

## 🔐 Auth Admin role permissions (`/v1/auth/admin/role-permissions`)

Nuevo endpoint para frontend de administración:

- `GET /v1/auth/admin/role-permissions?roleId=<uuid>`
- Responde permisos efectivos por rol en formato `moduleKey -> {read, write, delete}`.
- Para el mapeo de claves se normalizan nombres de módulo, incluyendo alias:
  - `Inventario` -> `INVENTORY`
  - `Core de Autenticación` -> `AUTH_CORE`

Ejemplo:

```json
{
  "roleId": "...",
  "roleName": "ADMIN",
  "permissions": {
    "INVENTORY": { "read": true, "write": true, "delete": true },
    "AUTH_CORE": { "read": true, "write": true, "delete": true }
  }
}
```

Además, `GET /v1/auth/admin/role-modules` ahora incluye `modules` con metadata básica (`id`, `key`, `name`) además de `moduleIds`.

## Sprint 1 Config + Org

Parámetros default por tenant (auto-seed idempotente):
- `sales.quote.requires_approval`
- `sales.quote.required_role`
- `doc.numbering.scope`
- `storage.mode`
- `inventory.expiry.enabled`

Nuevos endpoints:
- Config: `/v1/config/currencies`, `/v1/config/taxes`, `/v1/config/parameters`, `/v1/config/payment-methods`, `/v1/config/document-types`.
- Org: `/v1/org/branches`, `/v1/org/branches/{branchId}/warehouses`, `/v1/org/warehouses/{id}`, `/v1/org/user/branches`, `/v1/org/me/branches`, `/v1/org/me/branches/{branchId}/validate`.

Todos los endpoints aplican aislamiento por tenant y soft delete.


## Sprint 2 Config + Org

Cambios principales:
- DTOs de Config/Org ahora exponen `active` y `updatedAt` para alinear contrato con frontend.
- Soft delete en catálogos cfg/org y asignaciones de sucursal ahora guarda `deletedBy` con el usuario actual (`JWTUtil.getCurrentUser()`).
- Nuevos endpoints para asignaciones usuario↔sucursal:
  - `GET /v1/org/user-branch-assignments?userId=&branchId=&page=&size=`
  - `POST /v1/org/user-branch-assignments` body: `{ "userId": "...", "branchId": "..." }`
  - `DELETE /v1/org/user-branch-assignments/{id}`

Reglas del endpoint de asignaciones:
- Debe enviarse al menos `userId` o `branchId` para listar.
- Si la asignación ya existe activa, responde conflicto (409).
- Si existe soft-deleted, se reactiva al crear.


## Sprint 3 Sucursal activa

Cambios principales:
- Nuevo endpoint `GET /v1/org/me/branches` para obtener sucursales activas del usuario autenticado (tenant-scoped, active, sin soft delete).
- Nuevo endpoint `GET /v1/org/me/branches/{branchId}/validate` para validar la sucursal activa seleccionada por el cliente.
  - `200` si la sucursal existe y está asignada activamente al usuario.
  - `403` si la sucursal existe en el tenant pero no pertenece al usuario.
  - `404` si la sucursal no existe en el tenant o está eliminada/inactiva.
- Se conserva `GET /v1/org/user/branches` por compatibilidad y reutiliza la misma lógica de negocio.
- Se agregó utilidad en `OrgService` (`validateCurrentUserBranch`) para que otros módulos validen el `branchId` actual de forma estándar.
