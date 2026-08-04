# Multi-tenancy

## Model

PostgreSQL **schema-per-tenant** with a shared catalog in the **`public`** schema (Flyway `db/migration/public`). Hibernate is configured with `multiTenancy: SCHEMA`.

Intended request pipeline:

```
HTTP request
  → JWTAuthFilter (auth + contexts)
  → TenantContext.setTenant(schema)   ← not wired yet
  → JPA / Hibernate query
  → TenantIdentifierResolver.resolveCurrentTenantIdentifier()
  → SchemaMultiTenantConnectionProvider / search_path
  → PostgreSQL
  → clear ThreadLocals
```

## Source of truth

| Concern | Path |
|---|---|
| ThreadLocal schema name | `auth/application/context/TenantContext.java` |
| Current user id | `auth/application/context/UserContext.java` |
| School context | `auth/application/context/SchoolContext.java` |
| Bind user context after JWT | `common/application/serviceimpl/AppContextService.java` |
| Hibernate tenant resolver | `common/application/component/TenantIdentifierResolver.java` |
| Connection provider | `auth/application/component/SchemaMultiTenantConnectionProvider.java` |
| DataSource / search_path init | `common/config/flyway/FlywayConfig.java` |
| Provision tenant schema | `common/application/serviceimpl/TenantMigrationService.java` (from `TenantServiceImpl` on create) |
| CORS header allowance | `SecurityConfig` — `X-Tenant-ID` allowed but **unused** in filters |

## Current runtime truth

- `TenantContext.setTenant(...)` is **never called**. Resolver falls back to default (`public` / configured default).
- JWT has **no tenant claim**; `AppContextService.createAuthContexts` sets **UserContext only**.
- Tenant create runs Flyway against `classpath:db/migration/tenant` — **folder has no SQL scripts**, so provisioned schemas stay empty of app tables.
- Older docs may say `master` schema; **migrations and code use `public`**.

## When implementing tenant routing

1. Resolve schema from JWT claim and/or validated `X-Tenant-ID` after auth.
2. Call `TenantContext.setTenant(schema)` early in the filter chain.
3. Clear contexts on request completion (filter/`oncePerRequest` finally).
4. Add tenant DDL under `src/main/resources/db/migration/tenant/`.
5. Update `docs/APPLICATION.md` §5.5 / §9 and this file.

More narrative (pool sizing, diagrams): [`DATABASE_README.md`](../../DATABASE_README.md) — reconcile any `master` references with `public`.
