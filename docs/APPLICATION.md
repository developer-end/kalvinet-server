# KalviNet Server — Application Overview

> **Living document — open to all.** Plain Markdown under `docs/`.  
> Any AI agent, IDE assistant (Cursor, VS Code/Copilot, Claude, Windsurf, JetBrains, etc.), or human may **read and edit** this file.  
> **Must be updated** whenever APIs, security, tenancy, migrations, or services change.  
> Last reviewed: **2026-08-04**

Canonical agent map (edit paths): [`AGENTS.md`](../AGENTS.md)  
Deep-dive notes: [`docs/implementation/`](./implementation/)  
Doc hub: [`docs/README.md`](./README.md)  
Database detail: [`DATABASE_README.md`](../DATABASE_README.md)

---

## 1. Purpose

KalviNet Server is the **multi-tenant school & institution management API**. It authenticates users (unified password JWT + Google OAuth2 sign-in), forces new accounts onto `ROLE_USER`, and exposes authenticated role-assignment APIs with a server-enforced permission matrix.

Base URL (local): `http://localhost:8081/erp`

---

## 2. Tech stack

| Layer | Choice |
|---|---|
| Runtime | Java **17**, Spring Boot **3.5.10** |
| API | Spring Web MVC, Validation, springdoc-openapi **2.8.5** |
| Security | Spring Security (stateless), OAuth2 Client (Google), JJWT **0.11.5** (HS256) |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL, Flyway (custom multi-schema) |
| Cache | Spring Cache + Caffeine (`auth_cache`) |
| Realtime | Raw WebSocket (`/ws`) |
| Build | Maven (`mvnw` / `mvnw.cmd`) |

Artifact: `app.school:administration:0.0.1-SNAPSHOT`  
Root package: `app.school.administration`

---

## 3. Runtime configuration (local)

| Setting | Value |
|---|---|
| Port | `8081` |
| Context path | `/erp` |
| Active profile | `local` → `application-local.yaml` |
| Datasource | `jdbc:postgresql://localhost:5454/kalvinet-local-dev` |
| Docker Postgres | `docker-compose.yml` — host port **5454** → container 5432 |
| CORS origins | `http://localhost:4200`, `http://localhost:3000` (env override) |
| Frontend URL (OAuth redirect) | `http://localhost:4200` |
| JWT | HS256 HMAC; access **15 days**; refresh **2 months** |
| Swagger UI | `http://localhost:8081/erp/swagger-ui.html` |

---

## 4. Module layout

```
app.school.administration/
├── KalviNetApplication.java
├── auth/          # identity, JWT sign-in, OAuth accounts, users/roles/tenants, role assignment
├── common/        # security, JWT filter, Flyway, cache, WebSocket, health, base CRUD
├── dashboard/     # dashboard config API
└── modules/
    └── school/    # first feature module (school CRUD)
```

---

## 5. Functional areas (current)

### 5.1 Authentication — `/api/v1/auth`

| Capability | Status |
|---|---|
| Unified sign in (username/password → JWTs; **no portal role required**) | Implemented (`POST /signIn`) |
| Sign up → **always** `ROLE_USER` (ignores client role field) | Implemented (`POST /signUp`) |
| Sign out / refresh endpoints | **Gap** — constants only |
| Google OAuth2 redirect sign-in | Partial — success handler redirects without issuing JWT |
| OAuth account CRUD | Implemented (`/api/v1/oAuth`) — still **public** (pre-existing) |

### 5.2 Role assignment (authenticated)

| Endpoint | Behavior |
|---|---|
| `GET /api/v1/user/assignableRoles` | Roles the caller may grant from **DB catalog** (`RoleAssignmentPolicy`); never includes `ROLE_IT` |
| `POST /api/v1/user/assignRole/{uuid}` | Soft-deactivates prior mappings, activates/creates target role; writes `role_assignment_audit`; evicts `auth_cache` |
| `GET /api/v1/user/search?q=&page=&size=` | Paginated user search for Role Assignment UI (**unscoped** — tenant routing not wired) |
| `GET /api/v1/role/list` | IT-only — list active roles |
| `POST /api/v1/role/create` | IT-only — register institutional role (not baseline) |

### Assignable matrix (vs catalog)

Baseline seed: `USER`, `OWNER`, `MANAGER`, `MANAGEMENT`, `IT`. Additional roles exist only after IT registers them.

| Caller | May assign |
|---|---|
| `ROLE_IT` | Any catalog role except `ROLE_IT` |
| `ROLE_MANAGER` | Catalog except `IT`, `OWNER`, `MANAGER`, `USER` |
| `ROLE_MANAGEMENT` / `ROLE_TEACHER` | Non-baseline institutional roles only |
| Others | none |

`ROLE_IT` is never assignable via API. Authorization uses DB-loaded authorities (JWT proves identity; `auth_cache` evicted on assign).

### 5.3 Identity catalog

| Resource | Base path | Notes |
|---|---|---|
| User | `/api/v1/user` | CRUD + search + assignRole + assignableRoles |
| Role | `/api/v1/role` (+ legacy `/api/v1/rloe`) | Catalog CRUD |
| Tenant / School / OAuth | existing paths | Unchanged |

### 5.4 Dashboard / health / WS

Unchanged: `GET /api/v1/dashboard/config`, `GET /api/health`, `DELETE /api/requests/{id}/cancel`, `WS /ws`.

### 5.5 Database & roles

| Capability | Status |
|---|---|
| Public Flyway V1–V3 | Implemented (tables; role seed deferred to V4) |
| **V4** — baseline roles only: USER, OWNER, MANAGER, MANAGEMENT, IT | Implemented |
| Role Registry API (`/role/list`, `/role/create`) — IT only | Implemented |
| **V5** — `role_assignment_audit` (`V5__role_assignment_audit.sql`) | Implemented |
| Existing / seeded roles | SUPER_ADMIN, ADMIN, USER, STUDENT, TEACHER, STAFF, ACCOUNTANT, MANAGER, MANAGEMENT, OWNER, IT |
| Tenant schema SQL | Still empty |
| `TenantContext.setTenant` at request time | Still **not wired** (accepted temporary gap for user search) |

---

## 6. Security model

| Piece | Behavior |
|---|---|
| `SecurityConfig` | CSRF off, STATELESS, CORS, OAuth2 + JWT filter |
| `AuthConstant.PUBLIC_ENDPOINTS` | Unchanged — **do not** add user assign/search/assignableRoles |
| `JWTAuthFilter` | Bearer → load `CustomUserDetails` from DB (via cache) → SecurityContext |
| Role change effect | Immediate for **API** auth (cache eviction); client UI may need re-sign-in if JWT claim used for display |

---

## 7. Core services (additions)

| Service | Responsibility |
|---|---|
| `RoleAssignmentPolicy` | Single matrix for assignableRoles + assignRole |
| `UserServiceImpl.assignRole` / `searchUsers` / `getAssignableRolesForCurrentUser` | Role assignment flows |
| `RoleAssignmentAuditEntity` | Audit who assigned what, when |
| `CustomUserDetailsServiceImpl` | DB authorities + `auth_cache` |

---

## 8. Known gaps / stubs (track here)

- Tenant scoping still non-functional (`TenantContext.setTenant` unused) — user search is global/public-schema.
- Client JWT claim can lag UI role display until re-sign-in; server authorization uses DB roles.
- OAuth success still does not issue JWT.
- `/signOut` / `/refresh` unused; plaintext password equality fallback remains.
- Broad `permitAll` on school/tenant/dashboard/oAuth CRUD (pre-existing).
- Package typos `infrastucture` / `persistance` unchanged.
- Empty tenant migrations.

---

## 9. Documentation map

| Doc | Role |
|---|---|
| **This file** | Living server overview |
| [`AGENTS.md`](../AGENTS.md) | Task → files |
| [`implementation/`](./implementation/) | Deep notes |
| [`DATABASE_README.md`](../DATABASE_README.md) | DB architecture |

## 10. Maintenance contract (required)

Update this file on every API/security/Flyway/behavior change; bump **Last reviewed**.
