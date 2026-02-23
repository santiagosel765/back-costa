# Auditoría completa backend Ferrisys (estado real del código)

## 1) Estado actual detectado

### 1.1 Seguridad y autenticación
- La API usa Spring Security stateless con JWT; solo `/v1/auth/login` y `/actuator/health` están en `permitAll`, el resto requiere autenticación.
- El filtro JWT valida firma/expiración, carga `UserDetails` y luego exige `tenant` en contexto para requests autenticadas.
- `@EnableMethodSecurity` está activo, por lo que sí existe control por `@PreAuthorize` en varios endpoints.

### 1.2 Multi-tenant (implementación actual)
- Existe entidad `tenant` y relación `auth_user.tenant_id`.
- El `tenant` se extrae del JWT claim `tenant_id` (o `tenantId` por compatibilidad) y se guarda en `TenantContext` por request.
- Inventario, módulos, roles y auditoría **sí** consultan por `tenant_id` en servicios/repositorios.
- Área comercial (`client`, `provider`, `quote`, `purchase`) **no** tiene `tenant_id` en entidades ni filtros por tenant en repositorios/servicios.

### 1.3 Control de acceso por rol
- El sistema construye authorities dinámicas desde `Role` + módulos activos (`MODULE_*`) en `CustomUserDetailsService`.
- Endpoints de inventario y administración IAM usan `@PreAuthorize` por rol/módulo.
- Endpoints comerciales (`/v1/clients`, `/v1/providers`, `/v1/quotes`, `/v1/purchases`) no tienen `@PreAuthorize` por rol: hoy basta estar autenticado y tener módulo habilitado por licencia.

### 1.4 Control por módulo/licencia
- Sí existe control por módulo vía anotación `@RequireModule` + `ModuleLicenseInterceptor` sobre `/v1/**`.
- Sí existe entidad de licencia: `module_license` con `tenant_id`, `module_id`, `enabled`, `start_at`, `end_at`, `expires_at`.
- `FeatureFlagServiceImpl` valida licencias por tenant y ventana temporal.
- Comportamiento permisivo importante: si un módulo no está en el mapa de licencias cacheado, `isModuleEnabled` devuelve `true` por defecto (`getOrDefault(..., Boolean.TRUE)`).

### 1.5 Auditoría
- Existe tabla/entidad `audit_event` y servicio para registrar eventos (`publish`) y consultar con filtro por tenant para no superadmin.
- Entidades de dominio heredan `Auditable` (timestamps), pero no hay `created_by/updated_by` a nivel global de entidades.

---

## 2) Respuestas solicitadas (A/B/C)

### A) ¿El sistema es realmente multi-tenant seguro?

**Respuesta corta:** Parcial. No es multi-tenant seguro extremo en todo el backend.

- **Sí** hay extracción de tenant desde JWT y enforcement de contexto en requests autenticadas.
- **Sí** hay aislamiento por tenant en inventario/roles/módulos/auditoría.
- **No** hay aislamiento por tenant en el dominio comercial (clientes, proveedores, compras, cotizaciones), por lo que existe riesgo real de fuga entre tenants en esas áreas.
- **No** hay RLS de PostgreSQL ni filtro global automático en JPA; depende de disciplina manual por servicio/repositorio.

### B) ¿Existe actualmente un sistema de licencias?

**Sí, pero incompleto para SaaS comercial robusto.**

- Hay entidad de licencias (`module_license`) y validación por módulo.
- No existen entidades `subscription` o `plan`.
- Sí se valida módulo activo para endpoints anotados con `@RequireModule`.
- La protección de endpoints está mezclada:
  - algunos por rol + módulo,
  - otros solo por módulo,
  - otros solo autenticados.
- No hay política centralizada de “plan activo” o “límites del plan” (usuarios, sucursales, etc.).

### C) JWT actual

**Contenido actual del token**
- `sub` = username.
- Claim `tenant_id` (si el usuario tiene tenant asignado).
- `iat` y `exp` (10 horas).

**No incluye**
- roles,
- módulos activos,
- versión de sesión, device/session id, jti para revocación, issuer/audience explícitos.

**Evaluación SaaS profesional**
- Es suficiente para un MVP interno.
- Para SaaS profesional vendible falta hardening: refresh tokens, revocación, rotación de secretos/keys (idealmente RSA/ECDSA + kid), claims de control de sesión y estrategia explícita de invalidación.

---

## 3) Riesgos críticos

1. **Fuga cross-tenant en dominio comercial** por ausencia de `tenant_id` + consultas `findAll/findById` globales.
2. **Administración IAM cross-tenant**: `AuthAdminController` usa `findAll/findById` sin filtrar tenant en usuarios/roles/módulos/licencias.
3. **Autorización desigual**: varios endpoints comerciales sin control por rol.
4. **Licenciamiento permisivo por defecto** cuando no hay registro de licencia para módulo.
5. **JWT sin revocación/refresh**: exposición mayor ante robo de token hasta expiración.

---

## 4) Vacíos SaaS detectados

- Falta modelo comercial de suscripciones: `plan`, `subscription`, límites y ciclo de facturación.
- Falta `tenant_module` explícito como snapshot contractual (aunque existe `module_license`, no cubre plan/facturación).
- Falta entidad `branch` (sucursal) y aislamiento `branch_id` para operación multi-sede.
- Falta política uniforme de autorización (RBAC + licencias + tenant + branch) para todos los endpoints.
- Falta estrategia backend para sincronizar “módulos visibles” con frontend basada en licencia/plan vigente.

---

## 5) Arquitectura SaaS recomendada

### 5.1 Entidades mínimas
- `plan`:
  - id, code, name, status
  - pricing, period (monthly/yearly)
  - límites (max_users, max_branches, max_storage, etc.)
- `subscription`:
  - id, tenant_id, plan_id, status (`trialing/active/past_due/canceled`)
  - start_at, current_period_start/end, canceled_at
  - provider_ref (Stripe/Paddle/etc.)
- `tenant_module`:
  - id, tenant_id, module_id
  - source (`PLAN`/`ADDON`/`TRIAL`/`MANUAL`), enabled
  - start_at, end_at
- `branch`:
  - id, tenant_id, code, name, status, address
- Extender entidades operativas con `tenant_id` obligatorio y `branch_id` donde aplique.

### 5.2 JWT ideal
- Access token corto (10–15 min):
  - `sub`, `tenant_id`, `branch_id` actual (opcional)
  - `roles` (o role ids)
  - `scopes/modules` resumidos
  - `jti`, `iat`, `exp`, `iss`, `aud`, `token_version`
- Refresh token persistido/rotado con revocación server-side.
- Firma asimétrica + rotación de keys (`kid`).

### 5.3 Validación de módulos por endpoint
- Mantener `@RequireModule` pero endurecer `FeatureFlagService` a **deny-by-default**.
- Añadir `@RequirePlanFeature("...")` para features no 1:1 con módulos.
- Centralizar en interceptor o AuthorizationManager:
  - validar tenant activo,
  - validar subscription activa,
  - validar módulo/feature habilitado,
  - luego RBAC (`@PreAuthorize`).

### 5.4 Evitar acceso frontend a módulos sin licencia
- Backend expone `/v1/auth/modules` solo desde `tenant_module`/`subscription` vigentes.
- Frontend renderiza menú desde ese endpoint.
- Igual mantener enforcement backend (nunca confiar solo en frontend).

### 5.5 Validación de plan activo
- Middleware por request:
  1) resolver tenant,
  2) cargar subscription cacheada (TTL corto),
  3) bloquear si `status != active/trialing` o vencida,
  4) aplicar límites (usuarios/sucursales/transacciones).

---

## 6) Cambios estructurales prioritarios

### Prioridad 0 (inmediato)
1. Agregar `tenant_id` a `bus_client`, `bus_provider`, `bus_quote`, `bus_purchase`, detalles, y filtrar en repositorios/servicios.
2. Reescribir `AuthAdminController` para operar con `tenantContextHolder.requireTenantId()` y repositorios `findBy...AndTenantId`.
3. Endurecer licencias: `FeatureFlagService.isModuleEnabled` en deny-by-default.
4. Aplicar `@PreAuthorize` por rol a endpoints comerciales.

### Prioridad 1
5. Introducir `plan` + `subscription` + `tenant_module` y flujo de activación/suspensión.
6. Añadir `branch` y `branch_id` en operaciones comerciales/inventario.
7. JWT con refresh/revocación/jti/token_version.

### Prioridad 2
8. Evaluar PostgreSQL RLS para capa extra anti-fuga.
9. Auditoría enriquecida con `created_by/updated_by` y trazabilidad por branch.

---

## 7) Roadmap técnico backend

### Fase 1 (2–3 semanas) — Contención de riesgo
- Migraciones de tenant en dominio comercial.
- Refactor repositorios/servicios/controladores a tenant-aware.
- Cobertura de tests de aislamiento cross-tenant.
- Hardening de licencias a deny-by-default.

### Fase 2 (3–5 semanas) — SaaS billing core
- Implementar `plan/subscription/tenant_module`.
- Guard de `subscription` activa por request.
- Endpoint de capacidades efectivas por tenant.

### Fase 3 (3–4 semanas) — Multi-sucursal
- Implementar `branch` + `branch_id` en entidades y políticas.
- Scope de usuario por sucursal (si aplica).

### Fase 4 (2–3 semanas) — Seguridad avanzada
- Refresh token rotation + revocación.
- Firma asimétrica y key rotation.
- Alertas de seguridad y telemetría IAM.

---

## 8) Evidencia técnica usada para esta auditoría

- Seguridad/JWT/Tenant: `SecurityConfig`, `JwtFilterRequest`, `JWTUtil`, `TenantResolver`, `TenantContextHolder`.
- RBAC/módulos/licencias: `CustomUserDetailsService`, `RequireModule`, `ModuleLicenseInterceptor`, `FeatureFlagServiceImpl`.
- Multi-tenant por dominio: entidades/repositorios/servicios de inventory vs business.
- IAM admin: `AuthAdminController`.
- Auditoría: `AuditEvent`, `AuditEventServiceImpl`.
- Esquema DB: migraciones `V1`, `V101`, `V102`, `V5`, `V103`.
