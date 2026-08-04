# KalviNet — Backend Contribution & Architecture Guide

Welcome to **KalviNet**, the cloud-based multi-tenant school management application built using **Spring Boot (`kalvi-net-server`)** and **Angular LTS (`kalvi-net-web-client`)**.

---

## Documentation (open for all agents & IDEs)

| Doc | Use |
|---|---|
| [`APPLICATION.md`](./APPLICATION.md) | Living server overview — update when functionality changes |
| [`README.md`](./README.md) | Documentation hub |
| [`../AGENTS.md`](../AGENTS.md) | Where to edit for AI agents / IDE assistants |
| [`implementation/`](./implementation/) | Implementation deep-dives |
| [`../DATABASE_README.md`](../DATABASE_README.md) | Database & Flyway detail |

There is **no vendor lock** on these docs. Cursor rules / Copilot / Claude entry files only point here.

---

## 1. Directory Layout

```
D:\projects-com\KaliNet\
├── kalvi-net-server/              → Spring Boot multi-tenant API
│   ├── src/main/java/app/school/administration/
│   │   ├── auth/                  → Identity, JWT, OAuth accounts, tenants
│   │   ├── common/                → Security, Flyway, cache, WebSocket, base CRUD
│   │   ├── dashboard/             → Dashboard config API
│   │   └── modules/school/        → Feature modules (school first)
│   ├── src/main/resources/
│   │   ├── application.yaml
│   │   ├── application-local.yaml
│   │   └── db/migration/{public,tenant}/
│   ├── docs/                      → APPLICATION.md, implementation, modules
│   ├── AGENTS.md                  → Universal AI/IDE edit map
│   ├── DATABASE_README.md         → DB architecture
│   └── docker-compose.yml         → Local Postgres
└── kalvi-net-web-client/          → Angular Nx frontend
```

---

## 2. Module boundaries

| Package | May depend on |
|---|---|
| `modules.*` | `common`, `auth` (contexts/entities as needed) |
| `dashboard` | `common`, `auth` repositories/services |
| `auth` | `common` |
| `common` | Prefer no dependency on feature modules |

Feature modules should follow: **controller → service interface → serviceimpl → repository/entity**.

Reuse `AppBaseService` / `AppBaseRepository` for standard CRUD instead of duplicating find/create/update/deActivate.

---

## 3. API conventions

- Context path: `/erp`
- Versioned REST: `/api/v1/{module}` via `AppModuleApi` / `AppApiVersion`
- Shared actions: `AppCommonEndPoint` (`/findById/{uuid}`, `/create`, `/update`, `/deActivate/{uuid}`)
- Auth actions: `AppAuthEndPoints` (`/signIn`, `/signUp`, …)
- Responses: prefer existing DTO / `AppResponse` patterns; field errors via `FieldErrorResponse` + `GlobalExceptionHandler`

When adding a module:

1. Add constant in `AppModuleApi` (or a dedicated module constant class).
2. Decide if it belongs in `AuthConstant.PUBLIC_ENDPOINTS` (default: **authenticated**).
3. Add Flyway scripts under `public` or `tenant` as appropriate.
4. Update `docs/APPLICATION.md` and `AGENTS.md`.

---

## 4. Security checklist

1. Do **not** add new public prefixes unless the endpoint must be anonymous.
2. Prefer JWT + `@PreAuthorize` / method security for protected resources (expand as needed).
3. Never commit real OAuth secrets or production JWT secrets — use env vars.
4. Clear ThreadLocals (`TenantContext`, `UserContext`, `SchoolContext`) on request completion when you touch them.

---

## 5. Local run

```bash
cd kalvi-net-server
docker-compose up -d postgres
.\mvnw.cmd spring-boot:run
```

Server: `http://localhost:8081/erp`  
Swagger: `http://localhost:8081/erp/swagger-ui.html`
