# Auditoría backend Sprint 1 + Sprint 2 (Config + Org)

## Checklist

1. **Contrato API / DTOs**
   - ✅ Config (`currencies`, `taxes`, `parameters`, `payment-methods`, `document-types`) expone `id`, `code`, `name`, `description` (cuando aplica), `active`, `updatedAt`.
   - ✅ `branches` expone `address`, `description`, `active`, `updatedAt`.
   - ✅ `warehouses` expone `description`, `branchId`, `active`, `updatedAt`.
   - ✅ `user-branch-assignments` expone `id`, `userId`, `branchId`, `active`, `updatedAt`.

2. **Multi-tenant**
   - ✅ Servicios de Config/Org usan `TenantContextHolder.requireTenantId()` en list/create/update/delete.
   - ✅ Queries de repositorio para lectura/listado filtran por `tenant_id` y excluyen soft-deleted (`deleted_at IS NULL`).
   - ✅ `tenant_id` no se recibe por request DTO; se asigna desde contexto en servicios.

3. **Soft delete**
   - ✅ `ConfigCatalogServiceImpl` y `OrgServiceImpl` en `delete*()` aplican:
     - `active = false`
     - `deletedAt = now`
     - `deletedBy = jwtUtil.getCurrentUser()`
   - ✅ Los listados excluyen soft-deleted (repositorios con `deletedAtIsNull`).

4. **Asignaciones usuario↔sucursal (`org_user_branch`)**
   - ✅ Endpoints confirmados:
     - `GET /v1/org/user-branch-assignments?userId=&page=&size=`
     - `GET /v1/org/user-branch-assignments?branchId=&page=&size=`
     - `POST /v1/org/user-branch-assignments`
     - `DELETE /v1/org/user-branch-assignments/{id}`
   - ✅ `400` si faltan `userId` y `branchId`.
   - ✅ `409` si asignación activa ya existe.
   - ✅ Si existía soft-deleted, el `POST` revive (`active=true`, `deletedAt/deletedBy=null`).

5. **RBAC / licenciamiento por módulo**
   - ✅ `@RequireModule("config")` en `ConfigController`.
   - ✅ `@RequireModule("org")` en `OrgController` (incluye assignments).

6. **Migraciones / Seeds**
   - ⚠️ No existía seed de catálogos Config por tenant para evitar UI vacía.
   - ✅ Se agregó migración nueva `V110__seed_cfg_defaults.sql` (sin modificar migraciones previas), con 3 registros mínimos por catálogo (`currency`, `tax`, `parameter`, `payment_method`, `document_type`) para tenant default/resuelto.

7. **Naming `updatedAt` vs `updated_at`**
   - ✅ DTOs usan `updatedAt`.
   - ✅ Mappers convierten `entity.updatedAt -> dto.updatedAt` en ISO string (`toString()` de `OffsetDateTime`).

---

## Endpoints confirmados

- `GET/POST/PUT/DELETE /v1/config/currencies`
- `GET/POST/PUT/DELETE /v1/config/taxes`
- `GET/POST/PUT/DELETE /v1/config/parameters`
- `GET/POST/PUT/DELETE /v1/config/payment-methods`
- `GET/POST/PUT/DELETE /v1/config/document-types`
- `GET/POST/PUT/DELETE /v1/org/branches`
- `GET/POST /v1/org/branches/{branchId}/warehouses`
- `PUT/DELETE /v1/org/warehouses/{id}`
- `GET/POST/DELETE /v1/org/user-branch-assignments`

---

## Archivos revisados

- `src/main/java/com/ferrisys/controller/ConfigController.java`
- `src/main/java/com/ferrisys/controller/OrgController.java`
- `src/main/java/com/ferrisys/service/config/impl/ConfigCatalogServiceImpl.java`
- `src/main/java/com/ferrisys/service/org/impl/OrgServiceImpl.java`
- `src/main/java/com/ferrisys/repository/CurrencyRepository.java`
- `src/main/java/com/ferrisys/repository/TaxRepository.java`
- `src/main/java/com/ferrisys/repository/ParameterRepository.java`
- `src/main/java/com/ferrisys/repository/PaymentMethodRepository.java`
- `src/main/java/com/ferrisys/repository/DocumentTypeRepository.java`
- `src/main/java/com/ferrisys/repository/BranchRepository.java`
- `src/main/java/com/ferrisys/repository/WarehouseRepository.java`
- `src/main/java/com/ferrisys/repository/UserBranchAssignmentRepository.java`
- `src/main/java/com/ferrisys/common/dto/config/*.java`
- `src/main/java/com/ferrisys/common/dto/org/*.java`
- `src/main/java/com/ferrisys/mapper/config/*.java`
- `src/main/java/com/ferrisys/mapper/org/*.java`
- `src/main/resources/db/migration/V108__create_cfg_org_tables.sql`
- `src/main/resources/db/migration/V109__seed_config_org_modules.sql`
- `src/main/resources/db/migration/V110__seed_cfg_defaults.sql`

---

## Verificación SQL sugerida

```sql
-- 1) tenant elegido para seed
SELECT id, name
FROM tenant
ORDER BY CASE WHEN LOWER(name) IN ('default', 'default-tenant') THEN 0 ELSE 1 END,
         created_at NULLS LAST,
         id
LIMIT 1;

-- 2) validar datos seed por catálogo
SELECT tenant_id, code, name, active, deleted_at FROM cfg_currency ORDER BY code;
SELECT tenant_id, code, name, rate, active, deleted_at FROM cfg_tax ORDER BY code;
SELECT tenant_id, code, name, value, active, deleted_at FROM cfg_parameter ORDER BY code;
SELECT tenant_id, code, name, active, deleted_at FROM cfg_payment_method ORDER BY code;
SELECT tenant_id, code, name, active, deleted_at FROM cfg_document_type ORDER BY code;
```

## Commit message sugerido

`chore(audit): validate sprint1/2 config-org contracts and add tenant default config seeds`
