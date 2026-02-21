# Auditoría Técnica Backend — Ferrisys Service

## A) Resumen ejecutivo

- El backend está organizado como **monolito modular** (auth + inventario + compras + cotizaciones + catálogos) con seguridad JWT stateless y method security activa (`@EnableMethodSecurity`).
- El control de acceso se basa en authorities derivadas de `Role -> AuthRoleModule -> AuthModule` con prefijo `MODULE_` y normalización de texto (sin tildes/caracteres especiales).
- Existe un esquema de **feature flags + licencias** por `tenant_id`, pero hoy el `tenant` real se toma de `user.id` (no existe entidad Tenant ni claim de tenant en JWT), por lo que la semántica multi-tenant está incompleta y con riesgo alto de fuga cross-tenant.
- Hay inconsistencias de enforcement: varios endpoints críticos (`/v1/modules`, `/v1/roles`, `/v1/auth/register`, `/v1/auth/change-password`) no tienen `@PreAuthorize` granular y dependen solo de “estar autenticado” o son públicos.
- El modelo de auditoría actual solo cubre `created_at/updated_at` en entidades que heredan `Auditable`; no existe `created_by/updated_by`, ni tabla/evento de auditoría operativa.
- La base técnica habilita evolución rápida, pero requiere endurecimiento urgente en seguridad, multi-tenant y gobernanza de módulos/licencias.

---

## B) Arquitectura y módulos

### B.1 Mapa de paquetes

- `controller`: exposición REST (`AuthRestController`, `AuthAdminController`, inventario, compras, cotizaciones, catálogos).
- `service` y `service.impl`: casos de uso de auth, inventory, módulos, roles, feature flags.
- `service.business.*`: dominio comercial (clientes/proveedores/compras/cotizaciones).
- `repository`: acceso JPA por agregado.
- `config.security`: `SecurityFilterChain`, JWT util/filter, `UserDetailsService`, method security.
- `common.entity.*`: entidades por dominio (`user`, `inventory`, `business`, `license`).
- `config.bootstrap`: semillas iniciales (admin, rol ADMIN, vínculo).
- `db/migration`: Flyway schema + seeds.

### B.2 Core auth vs otros dominios

**Core auth / IAM**
- `auth_user`, `auth_role`, `auth_module`, `auth_user_role`, `auth_role_module`, `user_status`.
- Endpoints `/v1/auth/*` + `/v1/auth/admin/*`.

**Dominios funcionales**
- Inventario: categorías/productos (`/v1/inventory/*`).
- Comercial: clientes/proveedores/cotizaciones/compras (`/v1/clients`, `/v1/providers`, `/v1/quotes`, `/v1/purchases`).
- Gestión técnica de catálogo de módulos/roles: `/v1/modules`, `/v1/roles`.

---

## C) Inventario de API

> Convenciones columnas: Tenant = uso explícito de contexto tenant en endpoint/servicio. FeatureFlag = verificación `FeatureFlagService`. License = verificación de licencia por tenant.

| Método | Ruta | Módulo | Security | Tenant | FeatureFlag | License | DTOs principales |
|---|---|---|---|---|---|---|---|
| GET | `/actuator/health` | Core | Público (permitAll) | No | No | No | - |
| POST | `/v1/auth/register` | Core Auth | Público (permitAll por ruta) | No | No | No | `RegisterRequest` -> `AuthResponse` |
| POST | `/v1/auth/login` | Core Auth | Público (permitAll por ruta) | No | No | No | `LoginRequest` -> `AuthResponse` |
| POST | `/v1/auth/change-password` | Core Auth | Autenticado global, sin `@PreAuthorize` específico | No | No | No | query params -> `AuthResponse` |
| GET | `/v1/auth/modules` | Core Auth | Autenticado global | Implícito (usa `user.id` como tenant) | Sí (en servicio) | Sí (en servicio) | `PageResponse<ModuleDTO>` |
| GET | `/v1/auth/admin/users` | Admin Auth | `@PreAuthorize`: FF core auth + `MODULE_CORE_DE_AUTENTICACION` o ADMIN | No | Sí | Sí (vía FF) | `AdminUserResponse` |
| GET | `/v1/auth/admin/users/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | `AdminUserResponse` |
| POST | `/v1/auth/admin/users` | Admin Auth | Igual class-level | No | Sí | Sí | `AdminUserRequest`/`RegisterRequest` |
| PUT | `/v1/auth/admin/users/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | `AdminUserRequest` |
| DELETE | `/v1/auth/admin/users/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | - |
| POST | `/v1/auth/admin/user-roles` | Admin Auth | Igual class-level | No | Sí | Sí | `UserRoleRequest` |
| GET | `/v1/auth/admin/roles` | Admin Auth | Igual class-level | No | Sí | Sí | `Role` |
| GET | `/v1/auth/admin/roles/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | `Role` |
| POST | `/v1/auth/admin/roles` | Admin Auth | Igual class-level | No | Sí | Sí | `AdminRoleRequest` |
| PUT | `/v1/auth/admin/roles/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | `AdminRoleRequest` |
| DELETE | `/v1/auth/admin/roles/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | - |
| GET | `/v1/auth/admin/modules` | Admin Auth | Igual class-level | No | Sí | Sí | `AuthModule` |
| GET | `/v1/auth/admin/modules/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | `AuthModule` |
| POST | `/v1/auth/admin/modules` | Admin Auth | Igual class-level | No | Sí | Sí | `AdminModuleRequest` |
| PUT | `/v1/auth/admin/modules/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | `AdminModuleRequest` |
| DELETE | `/v1/auth/admin/modules/{id}` | Admin Auth | Igual class-level | No | Sí | Sí | - |
| GET | `/v1/auth/admin/role-modules` | Admin Auth | Igual class-level | No | Sí | Sí | `RoleModulesDto` |
| POST | `/v1/auth/admin/role-modules` | Admin Auth | Igual class-level | No | Sí | Sí | `RoleModuleRequest` |
| PUT | `/v1/auth/admin/role-modules/{roleId}` | Admin Auth | Igual class-level | No | Sí | Sí | `RoleModulesDto` |
| GET | `/v1/auth/admin/module-licenses` | Admin Auth | Igual class-level | Sí (tenantId en entidad licencia) | Sí | Sí | `ModuleLicense` |
| POST | `/v1/auth/admin/module-licenses` | Admin Auth | Igual class-level | Sí (tenantId request) | Sí | Sí | `ModuleLicenseRequest` |
| POST | `/v1/inventory/category/save` | Inventory | `@PreAuthorize`: FF inventory + authority MODULE_INVENTORY o ADMIN | No | Sí | Sí | `CategoryDTO` |
| POST | `/v1/inventory/product/save` | Inventory | Igual | No | Sí | Sí | `ProductDTO` |
| POST | `/v1/inventory/category/disable` | Inventory | Igual | No | Sí | Sí | query `id` |
| POST | `/v1/inventory/product/disable` | Inventory | Igual | No | Sí | Sí | query `id` |
| GET | `/v1/inventory/categories` | Inventory | Igual | No | Sí | Sí | `PageResponse<CategoryDTO>` |
| GET | `/v1/inventory/products` | Inventory | Igual | No | Sí | Sí | `PageResponse<ProductDTO>` |
| POST | `/v1/clients/save` | Clients | `@PreAuthorize` FF clients | No | Sí | Sí | `ClientDTO` |
| POST | `/v1/clients/disable` | Clients | `@PreAuthorize` FF clients | No | Sí | Sí | query `id` |
| GET | `/v1/clients/list` | Clients | `@PreAuthorize` FF clients | No | Sí | Sí | `PageResponse<ClientDTO>` |
| POST | `/v1/providers/save` | Providers | `@PreAuthorize` FF providers | No | Sí | Sí | `ProviderDTO` |
| POST | `/v1/providers/disable` | Providers | `@PreAuthorize` FF providers | No | Sí | Sí | query `id` |
| GET | `/v1/providers/list` | Providers | `@PreAuthorize` FF providers | No | Sí | Sí | `PageResponse<ProviderDTO>` |
| POST | `/v1/purchases/save` | Purchases | `@PreAuthorize` FF purchases | No | Sí | Sí | `PurchaseDTO` |
| POST | `/v1/purchases/disable` | Purchases | `@PreAuthorize` FF purchases | No | Sí | Sí | query `id` |
| GET | `/v1/purchases/list` | Purchases | `@PreAuthorize` FF purchases | No | Sí | Sí | `PageResponse<PurchaseDTO>` |
| POST | `/v1/quotes/save` | Quotes | `@PreAuthorize` FF quotes | No | Sí | Sí | `QuoteDTO` |
| POST | `/v1/quotes/disable` | Quotes | `@PreAuthorize` FF quotes | No | Sí | Sí | query `id` |
| GET | `/v1/quotes/list` | Quotes | `@PreAuthorize` FF quotes | No | Sí | Sí | `PageResponse<QuoteDTO>` |
| POST | `/v1/modules/save` | Auth Catalog | Solo autenticado global (sin `@PreAuthorize`) | No | No | No | `ModuleDTO` |
| GET | `/v1/modules/list` | Auth Catalog | Solo autenticado global | No | No | No | `PageResponse<ModuleDTO>` |
| POST | `/v1/modules/disable` | Auth Catalog | Solo autenticado global | No | No | No | query `id` |
| POST | `/v1/roles/save` | Auth Catalog | Solo autenticado global | No | No | No | `RoleDTO` |
| POST | `/v1/roles/list` | Auth Catalog | Solo autenticado global | No | No | No | `PageResponse<RoleDTO>` |
| POST | `/v1/roles/disable` | Auth Catalog | Solo autenticado global | No | No | No | query `roleId` |

---

## D) Seguridad

### D.1 Configuración actual

- `SecurityFilterChain`:
  - CSRF deshabilitado.
  - CORS habilitado con orígenes explícitos (`localhost:4200`, `clarifyerp...`).
  - Rutas públicas: `/v1/auth/login`, `/v1/auth/register`, `/actuator/health`.
  - Resto autenticado (`anyRequest().authenticated()`).
  - Sesión stateless.
  - JWT filter antes de `UsernamePasswordAuthenticationFilter`.

- JWT:
  - `sub=username`, expiración fija 10 horas.
  - Sin claims de tenant, roles o módulos.
  - Validación: sujeto + expiración.
  - Sin refresh token/revocación/blacklist.

- Authorities:
  - `ROLE_<ROL_NORMALIZADO>` desde role principal del usuario.
  - `MODULE_<MODULO_NORMALIZADO>` desde módulos del rol si `featureFlagService.enabled(user.id, moduleName)`.
  - Normalización elimina tildes y símbolos.

### D.2 Hallazgos/riesgos

1. **Superficie administrativa sin autorización granular** en `/v1/modules/*` y `/v1/roles/*`: cualquier usuario autenticado puede modificar catálogo de seguridad.
2. **Cambio de contraseña inseguro**: endpoint recibe `userToken` por query param y no exige `@PreAuthorize` explícito; mayor riesgo de exposición en logs/proxies.
3. **Register abierto**: permite crear usuarios libremente (válido en B2C, riesgoso en SaaS B2B sin invitación).
4. **Doble configuración CORS** (`SecurityConfig` + `WebCorsConfig`) potencial de divergencia.
5. **Sin hardening de token lifecycle**: no hay refresh token, rotate keys, ni invalidación por logout.
6. **Control por feature flags inconsistente**: algunos endpoints críticos no evalúan flags/licencias.

### D.3 Coherencia backend vs frontend

- El frontend podría ocultar opciones de módulos por permisos, pero backend no protege uniformemente `/v1/modules` y `/v1/roles`; hay riesgo de bypass por cliente API directo.

---

## E) Multi-tenant

### E.1 Estado actual observado

- No existe entidad `Tenant` ni `TenantContext`.
- JWT no incluye `tenantId` claim.
- `FeatureFlagService` evalúa por `tenantId`, pero `enabledForCurrentUser` usa **`user.id` como tenantId**.
- Repositorios de negocio (`client`, `provider`, `quote`, `purchase`, `inventory`) no filtran por tenant.
- Entidades de negocio no tienen `tenant_id` (salvo `module_license.tenant_id`; `inv_product` usa `company_id`, no aplicado en filtros).

### E.2 Riesgo

- Riesgo **alto** de data leak cross-tenant si se usa la misma BD para múltiples tenants: consultas `findAll()` retornan global.

### E.3 Patrón robusto recomendado (sin implementar)

- **Opción recomendada incremental**: shared DB/shared schema + `tenant_id` obligatorio en tablas de negocio + `TenantContext` por request (JWT claim o header firmado) + Hibernate Filter/Specifications globales + tests de aislamiento.
- **Opción de mayor aislamiento**: schema-per-tenant con routing datasource (más compleja operativamente).

---

## F) Feature flags y licencias

### F.1 Evaluación actual

- Servicio central `FeatureFlagServiceImpl` combina:
  1) toggle en `application.yml` (`modules.<slug>.enabled`),
  2) lookup de `AuthModule` por nombre normalizado,
  3) lookup de `module_license` por `tenant_id + module_id`,
  4) validación `enabled` y `expiresAt`.

### F.2 Fortalezas

- Normalización de slugs/nombres consistente con authorities (`sin tildes`, mayúsculas).
- Enforcement reusable en `@PreAuthorize` (`enabledForCurrentUser`).

### F.3 Gaps

- Semántica de tenant incorrecta (`user.id` usado como tenant).
- Módulos/roles sin enforcement de licencias/feature flags.
- Si módulo no existe en catálogo, `enabled()` puede devolver `propertyEnabled=true` (comportamiento permisivo).

---

## G) Auditoría de cambios (gap + propuesta)

### G.1 Estado actual

- Solo existe `Auditable` con `created_at` y `updated_at` por callbacks JPA (`@PrePersist/@PreUpdate`) para entidades que heredan esa clase.
- No se observa `created_by`, `updated_by`, tabla de audit log, ni interceptores/AOP dedicados.
- `module_license` usa trigger SQL para `updated_at`, pero entidad no hereda `Auditable`.

### G.2 Enfoque A (recomendado): auditoría de entidad + audit log

- Agregar `created_by`, `updated_by` con `AuditorAware` y Spring Data JPA auditing.
- Tabla `audit_event` (actor, tenant, acción, entidad, entity_id, before/after resumido, timestamp, request_id).
- Emisión de eventos en casos críticos (`AuthAdminController`, cambios de roles/módulos/licencias/usuarios).

**Pros**: bajo impacto, trazabilidad operativa, consulta simple.
**Contras**: snapshots parciales si no se diseña bien `before/after`.
**Esfuerzo**: medio (2-4 semanas).

### G.3 Enfoque B: event sourcing ligero (solo admin)

- Registrar comandos/eventos inmutables para operaciones de administración IAM/licencias.
- Materializar proyección para lectura administrativa.

**Pros**: historial completo, reversión/replay más fácil.
**Contras**: mayor complejidad conceptual y operativa.
**Esfuerzo**: alto (1-3 meses).

---

## H) Riesgos y deuda técnica

### Críticos

1. Falta de autorización granular en `/v1/modules` y `/v1/roles`.
2. Multi-tenant incompleto (sin aislamiento de datos por tenant).
3. Cambio de contraseña por query params + token en URL.
4. Ausencia de refresh/revocation strategy JWT.

### Relevantes

5. Excepciones genéricas `RuntimeException` en múltiples servicios.
6. DTOs sin validación (`@Valid`, `@NotBlank`, etc.).
7. Ausencia de pruebas automáticas (no se encontró `src/test`).
8. Doble configuración CORS.
9. Seed admin con credenciales conocidas en bootstrap/migrations.

### Quick wins

- Bloquear `/v1/modules` y `/v1/roles` con `@PreAuthorize` ADMIN/MODULE_CORE.
- Mover `change-password` a body DTO y quitar token de query.
- Estandarizar manejo de excepciones de dominio.
- Añadir validaciones Bean Validation en DTOs críticos.

---

## I) Backlog priorizado

### Horizonte 1–2 semanas

| Prioridad | Épica | Historia | Impacto | Riesgo | Complejidad | Dependencias | Criterios de aceptación |
|---|---|---|---|---|---|---|---|
| P0 | Seguridad IAM | Restringir `/v1/modules` y `/v1/roles` a ADMIN/MODULE_CORE | Alto | Alto | Baja | Ninguna | Endpoints devuelven 403 a usuario no autorizado; tests de autorización pasan |
| P0 | Seguridad credenciales | Rediseñar `change-password` para body JSON y auth robusta | Alto | Alto | Media | Frontend | No token en URL/logs; flujo validado con tests |
| P1 | Hardening JWT | Definir estrategia refresh token + expiración corta access token | Alto | Medio | Media | Diseño seguridad | Documento ADR + endpoints definidos + pruebas de expiración |
| P1 | Calidad API | Incorporar Bean Validation en DTOs auth/admin | Medio | Medio | Baja | Ninguna | Errores 400 estructurados para payload inválido |
| P1 | Observabilidad | Introducir request-id/correlation-id en logs | Medio | Bajo | Baja | Ninguna | Logs incluyen request-id en operaciones críticas |

### Horizonte 1 mes

| Prioridad | Épica | Historia | Impacto | Riesgo | Complejidad | Dependencias | Criterios de aceptación |
|---|---|---|---|---|---|---|---|
| P0 | Multi-tenant base | Definir TenantContext (claim/header), contrato y propagación | Alto | Alto | Media | Seguridad JWT | Tenant presente en request autenticado y accesible en capa servicio |
| P0 | Aislamiento datos | Añadir `tenant_id` a entidades negocio y filtros globales repositorio | Muy alto | Alto | Alta | TenantContext | Tests de aislamiento cross-tenant verdes |
| P1 | Feature/Licensing | Corregir `enabledForCurrentUser` para usar tenant real | Alto | Alto | Media | TenantContext | Licencias se evalúan por tenant real, no por userId |
| P1 | Excepciones dominio | Reemplazar `RuntimeException` por excepciones tipadas | Medio | Medio | Media | Ninguna | Cobertura de handlers y mensajes consistentes |
| P2 | CORS cleanup | Unificar estrategia CORS en una sola configuración | Bajo | Bajo | Baja | Ninguna | Config única y pruebas de preflight exitosas |

### Horizonte 3 meses

| Prioridad | Épica | Historia | Impacto | Riesgo | Complejidad | Dependencias | Criterios de aceptación |
|---|---|---|---|---|---|---|---|
| P1 | Auditoría operativa | Implementar Enfoque A: `created_by/updated_by` + `audit_event` | Alto | Medio | Alta | TenantContext + seguridad | Eventos críticos auditados y consultables |
| P2 | Admin event log | Pilotear Enfoque B en operaciones admin IAM | Medio | Medio | Alta | Auditoría base | Historial inmutable de comandos admin |
| P1 | Testing estrategia | Suite de tests seguridad, multi-tenant y contratos API | Alto | Medio | Alta | Refactors previos | Cobertura mínima acordada y pipeline en verde |
| P2 | Arquitectura evolutiva | Evaluar separación por bounded contexts (IAM vs negocio) | Medio | Medio | Alta | Estabilidad funcional | ADR de arquitectura + plan de migración por etapas |

---

## J) Checklist de QA para futuras refactors

1. **AuthN/AuthZ**
   - [ ] Cada endpoint mutable tiene `@PreAuthorize` explícito.
   - [ ] Tests 401/403 por rol/módulo.
   - [ ] Ningún token sensible en query params.

2. **Tenant isolation**
   - [ ] Toda entidad de negocio incluye `tenant_id` (o estrategia formal alternativa).
   - [ ] Toda query productiva filtra tenant.
   - [ ] Pruebas de no-fuga entre tenant A/B.

3. **Feature flags/licencias**
   - [ ] Enforcement server-side en controller/service para cada módulo.
   - [ ] Caducidad de licencia cubierta por tests.
   - [ ] Módulos inexistentes no habilitan por default en producción.

4. **Datos y auditoría**
   - [ ] `created_at/updated_at/created_by/updated_by` completos.
   - [ ] Eventos críticos registrados en `audit_event`.
   - [ ] Trazabilidad request-id -> evento -> actor.

5. **Calidad API**
   - [ ] DTOs con Bean Validation.
   - [ ] Errores homogéneos en `GlobalExceptionHandler`.
   - [ ] Contratos OpenAPI actualizados.

6. **Operación**
   - [ ] Rotación de secrets JWT y política de expiración.
   - [ ] Seed de admin controlada por entorno (no credencial conocida en prod).
   - [ ] Métricas de seguridad y auditoría observables.

---

## Fuentes clave revisadas

- Seguridad: `SecurityConfig`, `JwtFilterRequest`, `JWTUtil`, `CustomUserDetailsService`.
- Feature/licensing: `FeatureFlagServiceImpl`, `ModuleLicense`, `ModuleLicenseRepository`.
- API: controllers bajo `src/main/java/com/ferrisys/controller`.
- Modelo: entidades en `common/entity/**` y migraciones Flyway.
- Bootstrap y defaults: `DatabaseBootstrap`, `application.yml`, `V*_*.sql`.

Si necesitas, en una segunda iteración puedo convertir este diagnóstico en un **ADR pack** (seguridad, multi-tenant, auditoría) sin tocar código.
